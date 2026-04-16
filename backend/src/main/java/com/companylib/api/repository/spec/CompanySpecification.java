package com.companylib.api.repository.spec;

import com.companylib.api.domain.dto.search.*;
import com.companylib.api.domain.entity.Company;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CompanySpecification {

    private CompanySpecification() {}

    public static boolean hasFuzzy(AdvancedSearchRequest req) {
        return req.conditions().stream()
            .anyMatch(c -> c.matchType() == MatchType.FUZZY);
    }

    public static Specification<Company> from(AdvancedSearchRequest req) {
        List<Specification<Company>> specs = req.conditions().stream()
            .filter(c -> c.field() != null && c.matchType() != MatchType.FUZZY)
            .map(CompanySpecification::toSpec)
            .toList();

        if (specs.isEmpty()) return Specification.where(null);

        return req.logic() == LogicType.AND
            ? specs.stream().reduce(Specification::and).orElse(Specification.where(null))
            : specs.stream().reduce(Specification::or).orElse(Specification.where(null));
    }

    private static Specification<Company> toSpec(CompanySearchCondition c) {
        CompanyFieldRegistry.validate(c.field(), c.matchType());
        CompanyFieldRegistry.FieldType type = CompanyFieldRegistry.ALLOWED_FIELDS.get(c.field());

        return switch (type) {
            case TEXT     -> textSpec(c);
            case LONG     -> longSpec(c);
            case INTEGER  -> integerSpec(c);
            case DATE     -> dateSpec(c);
            case DATETIME -> dateTimeSpec(c);
        };
    }

    private static Specification<Company> textSpec(CompanySearchCondition c) {
        return (root, query, cb) -> {
            Path<String> path = root.get(c.field());
            String val = c.value();
            if (val == null || val.isBlank()) return cb.conjunction();
            // PREFIX/CONTAINS/SUFFIX はすべて LOWER() で正規化し、
            // V23 マイグレーションで追加した lower(col) GIN インデックスを利用する
            return switch (c.matchType()) {
                case EXACT    -> cb.equal(path, val);
                case PREFIX   -> cb.like(cb.lower(path), val.toLowerCase() + "%");
                case SUFFIX   -> cb.like(cb.lower(path), "%" + val.toLowerCase());
                case CONTAINS -> cb.like(cb.lower(path), "%" + val.toLowerCase() + "%");
                default       -> cb.conjunction();
            };
        };
    }

    private static Specification<Company> longSpec(CompanySearchCondition c) {
        return (root, query, cb) -> {
            if (c.value() == null) return cb.conjunction();
            try {
                long val = Long.parseLong(c.value());
                Path<Long> path = root.get(c.field());
                return switch (c.matchType()) {
                    case EQ   -> cb.equal(path, val);
                    case NEQ  -> cb.notEqual(path, val);
                    case GTE  -> cb.greaterThanOrEqualTo(path, val);
                    case LTE  -> cb.lessThanOrEqualTo(path, val);
                    case RANGE -> {
                        long to = Long.parseLong(c.valueTo());
                        yield cb.between(path, val, to);
                    }
                    default -> cb.conjunction();
                };
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("フィールド '%s' の値が数値ではありません: %s".formatted(c.field(), c.value()));
            }
        };
    }

    private static Specification<Company> integerSpec(CompanySearchCondition c) {
        return (root, query, cb) -> {
            if (c.value() == null) return cb.conjunction();
            try {
                int val = Integer.parseInt(c.value());
                Path<Integer> path = root.get(c.field());
                return switch (c.matchType()) {
                    case EQ   -> cb.equal(path, val);
                    case NEQ  -> cb.notEqual(path, val);
                    case GTE  -> cb.greaterThanOrEqualTo(path, val);
                    case LTE  -> cb.lessThanOrEqualTo(path, val);
                    case RANGE -> {
                        int to = Integer.parseInt(c.valueTo());
                        yield cb.between(path, val, to);
                    }
                    default -> cb.conjunction();
                };
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("フィールド '%s' の値が整数ではありません: %s".formatted(c.field(), c.value()));
            }
        };
    }

    private static Specification<Company> dateSpec(CompanySearchCondition c) {
        return (root, query, cb) -> {
            if (c.value() == null) return cb.conjunction();
            try {
                LocalDate val = LocalDate.parse(c.value());
                Path<LocalDate> path = root.get(c.field());
                return switch (c.matchType()) {
                    case EQ   -> cb.equal(path, val);
                    case NEQ  -> cb.notEqual(path, val);
                    case GTE  -> cb.greaterThanOrEqualTo(path, val);
                    case LTE  -> cb.lessThanOrEqualTo(path, val);
                    case RANGE -> {
                        LocalDate to = LocalDate.parse(c.valueTo());
                        yield cb.between(path, val, to);
                    }
                    default -> cb.conjunction();
                };
            } catch (Exception e) {
                throw new IllegalArgumentException("フィールド '%s' の値が日付（YYYY-MM-DD形式）ではありません: %s".formatted(c.field(), c.value()));
            }
        };
    }

    private static Specification<Company> dateTimeSpec(CompanySearchCondition c) {
        return (root, query, cb) -> {
            if (c.value() == null) return cb.conjunction();
            try {
                LocalDateTime val = LocalDateTime.parse(c.value());
                Path<LocalDateTime> path = root.get(c.field());
                return switch (c.matchType()) {
                    case EQ   -> cb.equal(path, val);
                    case NEQ  -> cb.notEqual(path, val);
                    case GTE  -> cb.greaterThanOrEqualTo(path, val);
                    case LTE  -> cb.lessThanOrEqualTo(path, val);
                    case RANGE -> {
                        LocalDateTime to = LocalDateTime.parse(c.valueTo());
                        yield cb.between(path, val, to);
                    }
                    default -> cb.conjunction();
                };
            } catch (Exception e) {
                throw new IllegalArgumentException("フィールド '%s' の日時形式が正しくありません: %s".formatted(c.field(), c.value()));
            }
        };
    }
}
