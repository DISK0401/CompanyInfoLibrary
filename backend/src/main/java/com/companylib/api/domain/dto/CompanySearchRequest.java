package com.companylib.api.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanySearchRequest {
    private String name;
    private String location;
    private Long minCapital;
    private Integer minEmployees;
    private int page = 0;
    private int size = 20;
}
