package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.Company;
import lombok.Getter;

@Getter
public class CompanySummaryDto {
    private final String corporateNumber;
    private final String name;
    private final String location;
    private final Long capitalStock;
    private final Integer employeeNumber;
    private final String representativeName;
    private final Integer foundingYear;

    public CompanySummaryDto(Company c) {
        this.corporateNumber = c.getCorporateNumber();
        this.name = c.getName();
        this.location = c.getLocation();
        this.capitalStock = c.getCapitalStock();
        this.employeeNumber = c.getEmployeeNumber();
        this.representativeName = c.getRepresentativeName();
        this.foundingYear = c.getFoundingYear();
    }
}
