package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyFinance;
import lombok.Getter;

@Getter
public class FinanceDetailDto {
    private final String period;
    private final String accountingStandards;
    private final Long netSales;
    private final Long grossOperatingRevenue;
    private final Long operatingRevenue1;
    private final Long operatingRevenue2;
    private final Long ordinaryIncome;
    private final Long ordinaryIncomeLoss;
    private final Long netIncomeLoss;
    private final Long netPremiumsWritten;
    private final Long totalAssets;
    private final Long netAssets;
    private final Long capitalStock;
    private final Integer numberOfEmployees;
    private final String fiscalYearCoverPage;
    private final String majorShareholders;

    public FinanceDetailDto(CompanyFinance entity) {
        this.period = entity.getPeriod();
        this.accountingStandards = entity.getAccountingStandards();
        this.netSales = entity.getNetSales();
        this.grossOperatingRevenue = entity.getGrossOperatingRevenue();
        this.operatingRevenue1 = entity.getOperatingRevenue1();
        this.operatingRevenue2 = entity.getOperatingRevenue2();
        this.ordinaryIncome = entity.getOrdinaryIncome();
        this.ordinaryIncomeLoss = entity.getOrdinaryIncomeLoss();
        this.netIncomeLoss = entity.getNetIncomeLoss();
        this.netPremiumsWritten = entity.getNetPremiumsWritten();
        this.totalAssets = entity.getTotalAssets();
        this.netAssets = entity.getNetAssets();
        this.capitalStock = entity.getCapitalStock();
        this.numberOfEmployees = entity.getNumberOfEmployees();
        this.fiscalYearCoverPage = entity.getFiscalYearCoverPage();
        this.majorShareholders = entity.getMajorShareholders();
    }
}
