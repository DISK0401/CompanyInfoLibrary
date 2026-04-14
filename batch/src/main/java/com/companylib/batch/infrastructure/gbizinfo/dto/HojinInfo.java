package com.companylib.batch.infrastructure.gbizinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * gBizINFO 法人情報 DTO。
 * Hojinjoho JSON ファイル（一括）および API v2 レスポンス（HojinInfoV2）の共通構造に対応。
 * 未知フィールドは {@link GBizInfoDeserializationProblemHandler} で WARN ログを出力して継続する。
 */
@Data
public class HojinInfo {

    // ── 基本情報 ──────────────────────────────────────────────────────────────

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
    private Long employeeNumber;

    @JsonProperty("company_size_male")
    private Long companySizeMale;

    @JsonProperty("company_size_female")
    private Long companySizeFemale;

    @JsonProperty("representative_name")
    private String representativeName;

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

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("industry")
    private List<String> industry;

    @JsonProperty("qualification_grade")
    private String qualificationGrade;

    /** 処理区分コード（01=新規, 11=商号変更, 12=所在地変更, 21=閉鎖 等） */
    @JsonProperty("process")
    private String process;

    /** 集計年（gBizINFO 内部管理用） */
    @JsonProperty("aggregated_year")
    private String aggregatedYear;

    /**
     * 法人レベルのメタデータ。
     * key_field（法人番号と同値）、data_quality/source/import_frequency/
     * last_acquisition_date/last_update_date（各フィールド単位の品質情報 Map）を含む。
     */
    @JsonProperty("meta-data")
    private Map<String, Object> companyMetadata;

    // ── 営業品目（事業種目コード文字列リスト） ─────────────────────────────────

    @JsonProperty("business_items")
    private List<String> businessItems;

    // ── 財務情報 ──────────────────────────────────────────────────────────────

    @JsonProperty("finance")
    private Finance finance;

    // ── 補助金情報 ────────────────────────────────────────────────────────────

    @JsonProperty("subsidy")
    private List<SubsidyInfo> subsidies;

    // ── 特許情報 ──────────────────────────────────────────────────────────────

    @JsonProperty("patent")
    private List<PatentInfo> patents;

    // ── 調達情報 ──────────────────────────────────────────────────────────────

    @JsonProperty("procurement")
    private List<ProcurementInfo> procurements;

    // ── 届出・認定情報 ────────────────────────────────────────────────────────

    @JsonProperty("certification")
    private List<CertificationInfo> certifications;

    // ── 表彰情報 ──────────────────────────────────────────────────────────────

    @JsonProperty("commendation")
    private List<CommendationInfo> commendations;

    // ── 職場情報 ──────────────────────────────────────────────────────────────

    @JsonProperty("workplace_info")
    private WorkplaceInfo workplaceInfo;

    // ─────────────────────────────────────────────────────────────────────────
    // Inner classes
    // ─────────────────────────────────────────────────────────────────────────

    // ── Finance ──────────────────────────────────────────────────────────────

    @Data
    public static class Finance {
        /** 会計基準（Finance レベル。ManagementIndex には存在しない） */
        @JsonProperty("accounting_standards")
        private String accountingStandards;

        /** 事業年度文字列（例: "第126期（自 2025年1月1日 至 2025年12月31日）"） */
        @JsonProperty("fiscal_year_cover_page")
        private String fiscalYearCoverPage;

        @JsonProperty("management_index")
        private List<ManagementIndex> managementIndex;

        @JsonProperty("major_shareholders")
        private List<MajorShareholder> majorShareholders;

        /** Finance レベルのメタデータ（key_field, data_quality, source 等）*/
        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    @Data
    public static class MajorShareholder {
        @JsonProperty("name_major_shareholders")
        private String name;

        @JsonProperty("shareholding_ratio")
        private Double shareholdingRatio;
    }

    @Data
    public static class ManagementIndex {
        @JsonProperty("period")
        private String period;

        @JsonProperty("net_sales_summary_of_business_results")
        private Long netSales;

        @JsonProperty("net_sales_summary_of_business_results_unit_ref")
        private String netSalesUnit;

        @JsonProperty("gross_operating_revenue_summary_of_business_results")
        private Long grossOperatingRevenue;

        @JsonProperty("gross_operating_revenue_summary_of_business_results_unit_ref")
        private String grossOperatingRevenueUnit;

        @JsonProperty("operating_revenue1_summary_of_business_results")
        private Long operatingRevenue1;

        @JsonProperty("operating_revenue1_summary_of_business_results_unit_ref")
        private String operatingRevenue1Unit;

        @JsonProperty("operating_revenue2_summary_of_business_results")
        private Long operatingRevenue2;

        @JsonProperty("operating_revenue2_summary_of_business_results_unit_ref")
        private String operatingRevenue2Unit;

        /** 経常収益（ordinary_income_loss とは別フィールド） */
        @JsonProperty("ordinary_income_summary_of_business_results")
        private Long ordinaryIncome;

        @JsonProperty("ordinary_income_summary_of_business_results_unit_ref")
        private String ordinaryIncomeUnit;

        @JsonProperty("ordinary_income_loss_summary_of_business_results")
        private Long ordinaryIncomeLoss;

        @JsonProperty("ordinary_income_loss_summary_of_business_results_unit_ref")
        private String ordinaryIncomeLossUnit;

        @JsonProperty("net_income_loss_summary_of_business_results")
        private Long netIncomeLoss;

        @JsonProperty("net_income_loss_summary_of_business_results_unit_ref")
        private String netIncomeLossUnit;

        /** 正味収入保険料（保険会社のみ。通常は null） */
        @JsonProperty("net_premiums_written_summary_of_business_results_ins")
        private Long netPremiumsWritten;

        @JsonProperty("net_premiums_written_summary_of_business_results_ins_unit_ref")
        private String netPremiumsWrittenUnit;

        @JsonProperty("capital_stock_summary_of_business_results")
        private Long capitalStock;

        @JsonProperty("capital_stock_summary_of_business_results_unit_ref")
        private String capitalStockUnit;

        @JsonProperty("net_assets_summary_of_business_results")
        private Long netAssets;

        @JsonProperty("net_assets_summary_of_business_results_unit_ref")
        private String netAssetsUnit;

        @JsonProperty("total_assets_summary_of_business_results")
        private Long totalAssets;

        @JsonProperty("total_assets_summary_of_business_results_unit_ref")
        private String totalAssetsUnit;

        @JsonProperty("number_of_employees")
        private Integer numberOfEmployees;

        @JsonProperty("number_of_employees_unit_ref")
        private String numberOfEmployeesUnit;
    }

    // ── SubsidyInfo ──────────────────────────────────────────────────────────

    @Data
    public static class SubsidyInfo {
        @JsonProperty("title")
        private String title;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("date_of_approval")
        private String dateOfApproval;

        @JsonProperty("government_departments")
        private String governmentDepartments;

        @JsonProperty("target")
        private String target;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    // ── PatentInfo ───────────────────────────────────────────────────────────

    @Data
    public static class PatentInfo {
        @JsonProperty("patent_type")
        private String patentType;

        @JsonProperty("title")
        private String title;

        @JsonProperty("registration_number")
        private String registrationNumber;

        @JsonProperty("application_date")
        private String applicationDate;

        /** 特許分類リスト。各要素は {"コード値": "35", "コード名": "類", "日本語": "..."} 形式 */
        @JsonProperty("classifications")
        private List<Map<String, String>> classifications;

        /** 文献固定アドレス（J-PlatPat リンク。将来的に提供予定、現在は null） */
        @JsonProperty("url")
        private String url;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    // ── ProcurementInfo ──────────────────────────────────────────────────────

    @Data
    public static class ProcurementInfo {
        @JsonProperty("title")
        private String title;

        @JsonProperty("amount")
        private Long amount;

        @JsonProperty("date_of_order")
        private String dateOfOrder;

        @JsonProperty("government_departments")
        private String governmentDepartments;

        @JsonProperty("note")
        private String note;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    // ── CertificationInfo ────────────────────────────────────────────────────

    @Data
    public static class CertificationInfo {
        @JsonProperty("title")
        private String title;

        @JsonProperty("category")
        private String category;

        @JsonProperty("date_of_approval")
        private String dateOfApproval;

        @JsonProperty("expire_date")
        private String expireDate;

        @JsonProperty("government_departments")
        private String governmentDepartments;

        @JsonProperty("target")
        private String target;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    // ── CommendationInfo ─────────────────────────────────────────────────────

    @Data
    public static class CommendationInfo {
        @JsonProperty("title")
        private String title;

        @JsonProperty("category")
        private String category;

        @JsonProperty("date_of_commendation")
        private String dateOfCommendation;

        @JsonProperty("government_departments")
        private String governmentDepartments;

        @JsonProperty("target")
        private String target;

        @JsonProperty("note")
        private String note;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    // ── WorkplaceInfo ─────────────────────────────────────────────────────────

    @Data
    public static class WorkplaceInfo {
        @JsonProperty("base_infos")
        private WorkplaceBaseInfos baseInfos;

        @JsonProperty("women_activity_infos")
        private WomenActivityInfos womenActivityInfos;

        @JsonProperty("compatibility_of_childcare_and_work")
        private CompatibilityOfChildcareAndWork compatibilityOfChildcareAndWork;

        @JsonProperty("meta-data")
        private ChildMetadata metadata;
    }

    @Data
    public static class WorkplaceBaseInfos {
        @JsonProperty("average_age")
        private Double averageAge;

        @JsonProperty("average_continuous_service_years")
        private Double averageContinuousServiceYears;

        @JsonProperty("average_continuous_service_years_Male")
        private Double averageContinuousServiceYearsMale;

        @JsonProperty("average_continuous_service_years_Female")
        private Double averageContinuousServiceYearsFemale;

        @JsonProperty("average_continuous_service_years_type")
        private String averageContinuousServiceYearsType;

        @JsonProperty("month_average_predetermined_overtime_hours")
        private Double monthAverageOvertimeHours;
    }

    @Data
    public static class WomenActivityInfos {
        @JsonProperty("female_workers_proportion")
        private Double femaleWorkersProportion;

        @JsonProperty("female_workers_proportion_type")
        private String femaleWorkersProportionType;

        @JsonProperty("female_share_of_manager")
        private Integer femaleShareOfManager;

        @JsonProperty("gender_total_of_manager")
        private Integer genderTotalOfManager;

        @JsonProperty("female_share_of_officers")
        private Integer femaleShareOfOfficers;

        @JsonProperty("gender_total_of_officers")
        private Integer genderTotalOfOfficers;
    }

    @Data
    public static class CompatibilityOfChildcareAndWork {
        @JsonProperty("paternity_leave_acquisition_num")
        private Integer paternityLeaveAcquisitionNum;

        @JsonProperty("number_of_paternity_leave")
        private Integer numberOfPaternityLeave;

        @JsonProperty("maternity_leave_acquisition_num")
        private Integer maternityLeaveAcquisitionNum;

        @JsonProperty("number_of_maternity_leave")
        private Integer numberOfMaternityLeave;
    }

    // ── ChildMetadata ─────────────────────────────────────────────────────────

    @Data
    public static class ChildMetadata {
        @JsonProperty("key_field")
        private String keyField;

        @JsonProperty("data_quality")
        private String dataQuality;

        @JsonProperty("source")
        private String source;

        @JsonProperty("import_frequency")
        private String importFrequency;

        @JsonProperty("last_acquisition_date")
        private String lastAcquisitionDate;

        @JsonProperty("last_update_date")
        private String lastUpdateDate;
    }
}
