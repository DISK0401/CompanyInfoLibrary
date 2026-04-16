package com.companylib.api.domain.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SortCondition(
    @NotBlank String field,
    @NotNull SortDirection direction
) {}
