package com.companylib.api.domain.dto.search;

public enum MatchType {
    // テキスト用
    EXACT, PREFIX, SUFFIX, CONTAINS, FUZZY,
    // 数値・日付用
    EQ, GTE, LTE, RANGE, NEQ
}
