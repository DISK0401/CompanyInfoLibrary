package com.companylib.api.domain.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "ソート条件の1件")
public record SortCondition(
    @Schema(description = "ソート対象フィールド名。CompanyFieldRegistryに定義されたフィールドのみ有効", example = "capitalStock")
    @NotBlank String field,

    @Schema(description = "ソート方向（ASC: 昇順 / DESC: 降順）")
    @NotNull SortDirection direction
) {}
