package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.Company;
import com.companylib.api.domain.entity.CompanyFinance;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Getter
public class CompanyDetailDto {
    private final String corporateNumber;
    private final String name;
    private final String kana;
    private final String nameEn;
    private final String postalCode;
    private final String location;
    private final String companyUrl;
    private final String businessSummary;
    private final Long capitalStock;
    private final Integer employeeNumber;
    private final String representativeName;
    private final String representativePosition;
    private final LocalDate dateOfEstablishment;
    private final Integer foundingYear;
    private final String status;
    private final List<String> businessItems;
    private final List<FinanceSummaryDto> finances;

    public CompanyDetailDto(Company c) {
        this.corporateNumber = c.getCorporateNumber();
        this.name = c.getName();
        this.kana = c.getKana();
        this.nameEn = c.getNameEn();
        this.postalCode = c.getPostalCode();
        this.location = c.getLocation();
        this.companyUrl = c.getCompanyUrl();
        this.businessSummary = c.getBusinessSummary();
        this.capitalStock = c.getCapitalStock();
        this.employeeNumber = c.getEmployeeNumber();
        this.representativeName = c.getRepresentativeName();
        this.representativePosition = c.getRepresentativePosition();
        this.dateOfEstablishment = c.getDateOfEstablishment();
        this.foundingYear = c.getFoundingYear();
        this.status = c.getStatus();
        this.businessItems = c.getBusinessItems().stream()
                .map(item -> item.getItemName())
                .toList();
        this.finances = c.getFinances().stream()
                .map(FinanceSummaryDto::new)
                .toList();
    }

    @Getter
    public static class FinanceSummaryDto {
        private final String period;
        private final Long netSales;
        private final Long netIncomeLoss;
        private final Long totalAssets;

        public FinanceSummaryDto(CompanyFinance f) {
            this.period = f.getPeriod();
            this.netSales = f.getNetSales();
            this.netIncomeLoss = f.getNetIncomeLoss();
            this.totalAssets = f.getTotalAssets();
        }
    }
}
