package com.companylib.api.domain.dto.search;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record AdvancedSearchRequest(
    @Valid List<CompanySearchCondition> conditions,
    LogicType logic,
    List<@Valid SortCondition> sort,
    @Min(0) int page,
    // @Min(1) @Max(100) はコンパクトコンストラクタのデフォルト補正より先に評価されないため、
    // バリデーションアノテーションを除去し、コンパクトコンストラクタで補正する方針に統一する
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
