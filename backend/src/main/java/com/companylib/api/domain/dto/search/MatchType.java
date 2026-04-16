package com.companylib.api.domain.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "検索マッチタイプ。テキスト用と数値・日付用がある")
public enum MatchType {
    @Schema(description = "完全一致（テキスト用）")
    EXACT,
    @Schema(description = "前方一致（テキスト用）")
    PREFIX,
    @Schema(description = "後方一致（テキスト用）")
    SUFFIX,
    @Schema(description = "部分一致（テキスト用）")
    CONTAINS,
    @Schema(description = "あいまい一致（テキスト用）")
    FUZZY,
    @Schema(description = "等値（数値・日付用）")
    EQ,
    @Schema(description = "以上（数値・日付用）")
    GTE,
    @Schema(description = "以下（数値・日付用）")
    LTE,
    @Schema(description = "範囲指定。valueが下限、valueToが上限（数値・日付用）")
    RANGE,
    @Schema(description = "不等値（数値・日付用）")
    NEQ
}
