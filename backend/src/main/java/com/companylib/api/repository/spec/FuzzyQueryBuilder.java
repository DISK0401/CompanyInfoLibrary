package com.companylib.api.repository.spec;

import com.companylib.api.domain.dto.search.*;
import com.companylib.api.domain.entity.Company;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FuzzyQueryBuilder {

    private static final double FUZZY_THRESHOLD = 0.3;
    private static final String DEFAULT_ORDER = "capital_stock DESC NULLS LAST";

    private final EntityManager entityManager;

    public FuzzyQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * FUZZY条件を含む複合検索を実行し、Page<Company> を返す。
     *
     * <p>制約: FUZZY + OR の組み合わせは {@link IllegalArgumentException} をスローする。
     */
    public Page<Company> search(AdvancedSearchRequest req, Pageable pageable) {
        boolean hasFuzzy = req.conditions().stream()
            .anyMatch(c -> c.matchType() == MatchType.FUZZY);

        if (hasFuzzy && req.logic() == LogicType.OR) {
            throw new IllegalArgumentException(
                "FUZZY条件とOR論理の組み合わせはサポートされていません。FUZZY条件を使用する場合はlogic=ANDを指定してください。");
        }

        // 各条件のバリデーション
        for (CompanySearchCondition condition : req.conditions()) {
            CompanyFieldRegistry.validate(condition.field(), condition.matchType());
        }

        Map<String, Object> params = new HashMap<>();
        AtomicInteger paramIndex = new AtomicInteger(0);

        String whereClause = buildWhereClause(req, params, paramIndex);
        String orderClause = buildOrderClause(req);

        String dataSQL = "SELECT * FROM companies" + whereClause + " ORDER BY " + orderClause;
        String countSQL = "SELECT COUNT(*) FROM companies" + whereClause;

        // countクエリ
        Query countQuery = entityManager.createNativeQuery(countSQL);
        setParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // データクエリ
        Query dataQuery = entityManager.createNativeQuery(dataSQL, Company.class);
        setParameters(dataQuery, params);
        long offset = pageable.getOffset();
        if (offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("ページオフセットが大きすぎます");
        }
        dataQuery.setFirstResult((int) offset);
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Company> results = dataQuery.getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    private String buildWhereClause(AdvancedSearchRequest req,
                                    Map<String, Object> params,
                                    AtomicInteger paramIndex) {
        List<String> fragments = new ArrayList<>();

        for (CompanySearchCondition condition : req.conditions()) {
            String fragment = toSqlFragment(condition, params, paramIndex);
            if (fragment != null && !fragment.isBlank()) {
                fragments.add(fragment);
            }
        }

        if (fragments.isEmpty()) {
            return "";
        }

        String joiner = req.logic() == LogicType.AND ? " AND " : " OR ";
        return " WHERE " + String.join(joiner, fragments);
    }

    /**
     * 1つの検索条件をSQLフラグメントに変換する。
     * 列名は CompanyFieldRegistry.toColumnName() を通したものだけ使用し、
     * 値は全て setParameter() で渡すためのプレースホルダー名を params に登録する。
     */
    private String toSqlFragment(CompanySearchCondition c,
                                  Map<String, Object> params,
                                  AtomicInteger paramIndex) {
        if (c.value() == null && c.matchType() != MatchType.FUZZY) {
            return null;
        }

        // SQLインジェクション防止: ホワイトリスト済みのフィールド名のみ使用
        String col = CompanyFieldRegistry.toColumnName(c.field());
        String paramName = "p" + paramIndex.getAndIncrement();

        return switch (c.matchType()) {
            case FUZZY -> {
                if (c.value() == null || c.value().isBlank()) {
                    yield null;
                }
                params.put(paramName, c.value());
                yield "similarity(" + col + ", :" + paramName + ") >= " + FUZZY_THRESHOLD;
            }
            case EXACT -> {
                params.put(paramName, c.value());
                yield col + " = :" + paramName;
            }
            case NEQ -> {
                params.put(paramName, c.value());
                yield col + " <> :" + paramName;
            }
            case PREFIX -> {
                params.put(paramName, c.value() + "%");
                yield col + " LIKE :" + paramName;
            }
            case SUFFIX -> {
                params.put(paramName, "%" + c.value());
                yield col + " LIKE :" + paramName;
            }
            case CONTAINS -> {
                params.put(paramName, "%" + c.value().toLowerCase() + "%");
                yield "LOWER(" + col + ") LIKE :" + paramName;
            }
            case GTE -> {
                params.put(paramName, parseTypedValue(c));
                yield col + " >= :" + paramName;
            }
            case LTE -> {
                params.put(paramName, parseTypedValue(c));
                yield col + " <= :" + paramName;
            }
            case EQ -> {
                params.put(paramName, parseTypedValue(c));
                yield col + " = :" + paramName;
            }
            case RANGE -> {
                String paramNameTo = "p" + paramIndex.getAndIncrement();
                params.put(paramName, parseTypedValue(c));
                params.put(paramNameTo, parseTypedValueTo(c));
                yield col + " BETWEEN :" + paramName + " AND :" + paramNameTo;
            }
        };
    }

    private Object parseTypedValue(CompanySearchCondition c) {
        CompanyFieldRegistry.FieldType type = CompanyFieldRegistry.ALLOWED_FIELDS.get(c.field());
        String val = c.value();
        if (val == null) return null;
        try {
            return switch (type) {
                case LONG     -> Long.parseLong(val);
                case INTEGER  -> Integer.parseInt(val);
                case DATE     -> java.time.LocalDate.parse(val);
                case DATETIME -> java.time.LocalDateTime.parse(val);
                case TEXT     -> val;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "フィールド '%s' の値が型 %s に変換できません: %s".formatted(c.field(), type, val));
        }
    }

    private Object parseTypedValueTo(CompanySearchCondition c) {
        CompanyFieldRegistry.FieldType type = CompanyFieldRegistry.ALLOWED_FIELDS.get(c.field());
        String val = c.valueTo();
        if (val == null) {
            throw new IllegalArgumentException(
                "フィールド '%s' のRANGE条件には valueTo が必要です".formatted(c.field()));
        }
        try {
            return switch (type) {
                case LONG     -> Long.parseLong(val);
                case INTEGER  -> Integer.parseInt(val);
                case DATE     -> java.time.LocalDate.parse(val);
                case DATETIME -> java.time.LocalDateTime.parse(val);
                case TEXT     -> val;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "フィールド '%s' の valueTo が型 %s に変換できません: %s".formatted(c.field(), type, val));
        }
    }

    private String buildOrderClause(AdvancedSearchRequest req) {
        if (req.sort() == null || req.sort().isEmpty()) {
            return DEFAULT_ORDER;
        }

        List<String> orderParts = new ArrayList<>();
        for (SortCondition sortCondition : req.sort()) {
            // SQLインジェクション防止: ホワイトリスト済みのフィールド名のみ使用
            if (!CompanyFieldRegistry.ALLOWED_FIELDS.containsKey(sortCondition.field())) {
                throw new IllegalArgumentException("不正なソートフィールド名: " + sortCondition.field());
            }
            String col = CompanyFieldRegistry.toColumnName(sortCondition.field());
            String dir = sortCondition.direction() == SortDirection.DESC ? "DESC" : "ASC";
            orderParts.add(col + " " + dir);
        }

        return String.join(", ", orderParts);
    }

    private void setParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }
}
