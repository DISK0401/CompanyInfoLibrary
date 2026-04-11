package com.companylib.api.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanySearchRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String location;

    @Min(0)
    private Long minCapital;

    @Min(0)
    private Integer minEmployees;

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;
}
