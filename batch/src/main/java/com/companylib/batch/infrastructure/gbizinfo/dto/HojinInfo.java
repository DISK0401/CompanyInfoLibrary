package com.companylib.batch.infrastructure.gbizinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HojinInfo {
    @JsonProperty("corporate_number")
    private String corporateNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("kana")
    private String kana;

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("location")
    private String location;

    @JsonProperty("company_url")
    private String companyUrl;

    @JsonProperty("business_summary")
    private String businessSummary;

    @JsonProperty("capital_stock")
    private Long capitalStock;

    @JsonProperty("employee_number")
    private Integer employeeNumber;

    @JsonProperty("company_size_male")
    private Integer companySizeMale;

    @JsonProperty("company_size_female")
    private Integer companySizeFemale;

    @JsonProperty("representative_name")
    private String representativeName;

    @JsonProperty("representative_position")
    private String representativePosition;

    @JsonProperty("date_of_establishment")
    private String dateOfEstablishment;

    @JsonProperty("founding_year")
    private Integer foundingYear;

    @JsonProperty("status")
    private String status;

    @JsonProperty("close_date")
    private String closeDate;

    @JsonProperty("close_cause")
    private String closeCause;

    @JsonProperty("update_date")
    private String updateDate;

    @JsonProperty("business_items")
    private List<BusinessItem> businessItems;

    @JsonProperty("finance")
    private Finance finance;

    @Data
    public static class BusinessItem {
        @JsonProperty("business_item")
        private String businessItem;
    }

    @Data
    public static class Finance {
        @JsonProperty("management_index")
        private List<ManagementIndex> managementIndex;
    }

    @Data
    public static class ManagementIndex {
        @JsonProperty("period")
        private String period;

        @JsonProperty("accounting_standards")
        private String accountingStandards;

        @JsonProperty("net_sales_summary_of_business_results")
        private Long netSales;

        @JsonProperty("gross_operating_revenue_summary_of_business_results")
        private Long grossOperatingRevenue;

        @JsonProperty("net_income_loss_summary_of_business_results")
        private Long netIncomeLoss;

        @JsonProperty("ordinary_income_loss_summary_of_business_results")
        private Long ordinaryIncomeLoss;

        @JsonProperty("total_assets_summary_of_business_results")
        private Long totalAssets;

        @JsonProperty("net_assets_summary_of_business_results")
        private Long netAssets;

        @JsonProperty("capital_stock_summary_of_business_results")
        private Long capitalStock;

        @JsonProperty("number_of_employees")
        private Integer numberOfEmployees;
    }
}
