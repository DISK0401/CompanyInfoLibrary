package com.companylib.api.domain.dto.search;

import java.util.Map;
import java.util.Set;

public final class CompanyFieldRegistry {

    public enum FieldType { TEXT, INTEGER, LONG, DATE, DATETIME }

    public static final Map<String, FieldType> ALLOWED_FIELDS = Map.ofEntries(
        Map.entry("corporateNumber",    FieldType.TEXT),
        Map.entry("name",               FieldType.TEXT),
        Map.entry("kana",               FieldType.TEXT),
        Map.entry("nameEn",             FieldType.TEXT),
        Map.entry("postalCode",         FieldType.TEXT),
        Map.entry("location",           FieldType.TEXT),
        Map.entry("companyUrl",         FieldType.TEXT),
        Map.entry("businessSummary",    FieldType.TEXT),
        Map.entry("status",             FieldType.TEXT),
        Map.entry("closeCause",         FieldType.TEXT),
        Map.entry("kind",               FieldType.TEXT),
        Map.entry("qualificationGrade", FieldType.TEXT),
        Map.entry("aggregatedYear",     FieldType.TEXT),
        Map.entry("process",            FieldType.TEXT),
        Map.entry("representativeName", FieldType.TEXT),
        Map.entry("capitalStock",       FieldType.LONG),
        Map.entry("employeeNumber",     FieldType.INTEGER),
        Map.entry("companySizeMale",    FieldType.INTEGER),
        Map.entry("companySizeFemale",  FieldType.INTEGER),
        Map.entry("foundingYear",       FieldType.INTEGER),
        Map.entry("dateOfEstablishment",FieldType.DATE),
        Map.entry("closeDate",          FieldType.DATE),
        Map.entry("gbizinfoUpdateDate", FieldType.DATE),
        Map.entry("createdAt",          FieldType.DATETIME),
        Map.entry("updatedAt",          FieldType.DATETIME)
    );

    private static final Set<MatchType> TEXT_OPS =
        Set.of(MatchType.EXACT, MatchType.PREFIX, MatchType.SUFFIX,
               MatchType.CONTAINS, MatchType.FUZZY);
    private static final Set<MatchType> NUMERIC_OPS =
        Set.of(MatchType.EQ, MatchType.GTE, MatchType.LTE, MatchType.RANGE, MatchType.NEQ);

    private CompanyFieldRegistry() {}

    public static void validate(String field, MatchType matchType) {
        FieldType type = ALLOWED_FIELDS.get(field);
        if (type == null) {
            throw new IllegalArgumentException("不正なフィールド名: " + field);
        }
        Set<MatchType> allowed = (type == FieldType.TEXT) ? TEXT_OPS : NUMERIC_OPS;
        if (!allowed.contains(matchType)) {
            throw new IllegalArgumentException(
                "フィールド '%s'（%s型）に対して '%s' は使用できません".formatted(field, type, matchType));
        }
    }

    /** JPA フィールド名 → DB スネークケース列名 */
    public static String toColumnName(String jpaField) {
        // 先頭文字は小文字化のみ、2文字目以降の大文字の前にアンダースコアを挿入
        return jpaField.substring(0, 1).toLowerCase()
            + jpaField.substring(1).replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}
