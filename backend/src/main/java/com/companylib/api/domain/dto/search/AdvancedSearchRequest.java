package com.companylib.api.domain.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "企業高度検索リクエスト")
public record AdvancedSearchRequest(
    @Schema(description = "検索条件リスト。空の場合は全件対象")
    @Valid List<CompanySearchCondition> conditions,

    @Schema(description = "検索条件の論理演算（AND / OR）。デフォルトはAND", defaultValue = "AND")
    LogicType logic,

    @Schema(description = "ソート条件リスト。空の場合はデフォルトソート（資本金降順）")
    List<@Valid SortCondition> sort,

    @Schema(description = "ページ番号（0始まり）", example = "0", defaultValue = "0")
    @Min(0) int page,

    // @Min(1) @Max(100) はコンパクトコンストラクタのデフォルト補正より先に評価されないため、
    // バリデーションアノテーションを除去し、コンパクトコンストラクタで補正する方針に統一する
    @Schema(description = "1ページあたりの件数（1〜100）。範囲外の場合は20に補正", example = "20", defaultValue = "20")
    int size
) {
    public AdvancedSearchRequest {
        if (conditions == null) conditions = List.of();
        if (logic == null) logic = LogicType.AND;
        if (sort == null) sort = List.of();
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;
    }
}
