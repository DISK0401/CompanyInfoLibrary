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
    private final Integer companySizeMale;
    private final Integer companySizeFemale;
    private final String representativeName;
    private final LocalDate dateOfEstablishment;
    private final Integer foundingYear;
    private final String status;
    private final LocalDate closeDate;
    private final String closeCause;
    private final LocalDate gbizinfoUpdateDate;
    private final String kind;
    private final String industry;
    private final String qualificationGrade;
    private final List<String> businessItems;
    private final List<FinanceDetailDto> finances;
    private final WorkplaceDto workplace;

    public CompanyDetailDto(Company c, WorkplaceDto workplace) {
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
        this.companySizeMale = c.getCompanySizeMale();
        this.companySizeFemale = c.getCompanySizeFemale();
        this.representativeName = c.getRepresentativeName();
        this.dateOfEstablishment = c.getDateOfEstablishment();
        this.foundingYear = c.getFoundingYear();
        this.status = c.getStatus();
        this.closeDate = c.getCloseDate();
        this.closeCause = c.getCloseCause();
        this.gbizinfoUpdateDate = c.getGbizinfoUpdateDate();
        this.kind = c.getKind();
        this.industry = c.getIndustry();
        this.qualificationGrade = c.getQualificationGrade();
        this.businessItems = c.getBusinessItems().stream()
                .map(item -> item.getItemName())
                .toList();
        this.finances = c.getFinances().stream()
                .sorted((a, b) -> {
                    if (a.getPeriod() == null) return 1;
                    if (b.getPeriod() == null) return -1;
                    return b.getPeriod().compareTo(a.getPeriod());
                })
                .map(FinanceDetailDto::new)
                .toList();
        this.workplace = workplace;
    }

    @Getter
    public static class FinanceDetailDto {
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

        public FinanceDetailDto(CompanyFinance f) {
            this.period = f.getPeriod();
            this.accountingStandards = f.getAccountingStandards();
            this.netSales = f.getNetSales();
            this.grossOperatingRevenue = f.getGrossOperatingRevenue();
            this.operatingRevenue1 = f.getOperatingRevenue1();
            this.operatingRevenue2 = f.getOperatingRevenue2();
            this.ordinaryIncome = f.getOrdinaryIncome();
            this.ordinaryIncomeLoss = f.getOrdinaryIncomeLoss();
            this.netIncomeLoss = f.getNetIncomeLoss();
            this.netPremiumsWritten = f.getNetPremiumsWritten();
            this.totalAssets = f.getTotalAssets();
            this.netAssets = f.getNetAssets();
            this.capitalStock = f.getCapitalStock();
            this.numberOfEmployees = f.getNumberOfEmployees();
            this.fiscalYearCoverPage = f.getFiscalYearCoverPage();
            this.majorShareholders = f.getMajorShareholders();
        }
    }
}
