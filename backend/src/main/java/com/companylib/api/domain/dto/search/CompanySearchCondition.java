package com.companylib.api.domain.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "検索条件の1件")
public record CompanySearchCondition(
    @Schema(description = "検索対象フィールド名。CompanyFieldRegistryに定義されたフィールドのみ有効", example = "name")
    @NotBlank String field,

    @Schema(description = "マッチタイプ。テキスト用: EXACT/PREFIX/SUFFIX/CONTAINS/FUZZY、数値・日付用: EQ/GTE/LTE/RANGE/NEQ")
    @NotNull MatchType matchType,

    @Schema(description = "検索値。RANGEの場合は下限値", example = "株式会社")
    String value,

    @Schema(description = "RANGE指定時の上限値", example = "100000000")
    String valueTo
) {}
