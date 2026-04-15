package com.companylib.api.domain.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanySearchCondition(
    @NotBlank String field,
    @NotNull MatchType matchType,
    String value,
    String valueTo
) {}
