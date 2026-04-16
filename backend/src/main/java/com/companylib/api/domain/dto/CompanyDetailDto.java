package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.Company;
import com.companylib.api.domain.entity.CompanyFinance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "企業詳細情報")
@Getter
public class CompanyDetailDto {

    @Schema(description = "法人番号（13桁）", example = "1234567890123")
    private final String corporateNumber;

    @Schema(description = "法人名", example = "株式会社サンプル")
    private final String name;

    @Schema(description = "法人名フリガナ", example = "カブシキカイシャサンプル")
    private final String kana;

    @Schema(description = "法人名（英語）", example = "Sample Corporation")
    private final String nameEn;

    @Schema(description = "郵便番号", example = "100-0001")
    private final String postalCode;

    @Schema(description = "本社所在地", example = "東京都千代田区丸の内1-1-1")
    private final String location;

    @Schema(description = "企業ホームページURL", example = "https://www.example.co.jp")
    private final String companyUrl;

    @Schema(description = "事業概要")
    private final String businessSummary;

    @Schema(description = "資本金（円）", example = "100000000")
    private final Long capitalStock;

    @Schema(description = "従業員数（全体）", example = "500")
    private final Integer employeeNumber;

    @Schema(description = "男性従業員数", example = "300")
    private final Integer companySizeMale;

    @Schema(description = "女性従業員数", example = "200")
    private final Integer companySizeFemale;

    @Schema(description = "代表者名", example = "山田 太郎")
    private final String representativeName;

    @Schema(description = "設立年月日", example = "1990-04-01")
    private final LocalDate dateOfEstablishment;

    @Schema(description = "創業年", example = "1990")
    private final Integer foundingYear;

    @Schema(description = "法人ステータス", example = "01")
    private final String status;

    @Schema(description = "閉鎖日")
    private final LocalDate closeDate;

    @Schema(description = "閉鎖事由")
    private final String closeCause;

    @Schema(description = "gBizINFO最終更新日", example = "2024-01-15")
    private final LocalDate gbizinfoUpdateDate;

    @Schema(description = "法人種別", example = "株式会社")
    private final String kind;

    @Schema(description = "業種", example = "情報通信業")
    private final String industry;

    @Schema(description = "経営事項審査の格付等", example = "A")
    private final String qualificationGrade;

    @Schema(description = "営業品目リスト")
    private final List<String> businessItems;

    @Schema(description = "財務情報一覧（年度降順）")
    private final List<FinanceDetailDto> finances;

    @Schema(description = "職場情報（情報がない場合はnull）")
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

    @Schema(description = "財務情報（年度別）")
    @Getter
    public static class FinanceDetailDto {

        @Schema(description = "回次（年度）", example = "第30期")
        private final String period;

        @Schema(description = "会計基準", example = "日本基準")
        private final String accountingStandards;

        @Schema(description = "売上高（円）", example = "5000000000")
        private final Long netSales;

        @Schema(description = "営業総収入（円）", example = "5000000000")
        private final Long grossOperatingRevenue;

        @Schema(description = "営業収益1（円）")
        private final Long operatingRevenue1;

        @Schema(description = "営業収益2（円）")
        private final Long operatingRevenue2;

        @Schema(description = "経常利益（円）", example = "300000000")
        private final Long ordinaryIncome;

        @Schema(description = "経常損失（円）")
        private final Long ordinaryIncomeLoss;

        @Schema(description = "当期純利益/損失（円）", example = "200000000")
        private final Long netIncomeLoss;

        @Schema(description = "正味収入保険料（円）")
        private final Long netPremiumsWritten;

        @Schema(description = "総資産額（円）", example = "10000000000")
        private final Long totalAssets;

        @Schema(description = "純資産額（円）", example = "3000000000")
        private final Long netAssets;

        @Schema(description = "資本金（円）", example = "100000000")
        private final Long capitalStock;

        @Schema(description = "従業員数", example = "500")
        private final Integer numberOfEmployees;

        @Schema(description = "有価証券報告書表紙URL")
        private final String fiscalYearCoverPage;

        @Schema(description = "大株主情報")
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
