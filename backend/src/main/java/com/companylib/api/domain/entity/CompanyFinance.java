package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_finances")
@Getter
@Setter
@NoArgsConstructor
public class CompanyFinance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_number", nullable = false)
    private Company company;

    @Column(name = "period", length = 20)
    private String period;

    @Column(name = "accounting_standards", length = 50)
    private String accountingStandards;

    // ── 金額フィールド ─────────────────────────────────────────────────────────

    @Column(name = "net_sales")
    private Long netSales;

    @Column(name = "net_sales_unit", length = 20)
    private String netSalesUnit;

    @Column(name = "gross_operating_revenue")
    private Long grossOperatingRevenue;

    @Column(name = "gross_operating_revenue_unit", length = 20)
    private String grossOperatingRevenueUnit;

    @Column(name = "operating_revenue1")
    private Long operatingRevenue1;

    @Column(name = "operating_revenue1_unit", length = 20)
    private String operatingRevenue1Unit;

    @Column(name = "operating_revenue2")
    private Long operatingRevenue2;

    @Column(name = "operating_revenue2_unit", length = 20)
    private String operatingRevenue2Unit;

    @Column(name = "ordinary_income")
    private Long ordinaryIncome;

    @Column(name = "ordinary_income_unit", length = 20)
    private String ordinaryIncomeUnit;

    @Column(name = "ordinary_income_loss")
    private Long ordinaryIncomeLoss;

    @Column(name = "ordinary_income_loss_unit", length = 20)
    private String ordinaryIncomeLossUnit;

    @Column(name = "net_income_loss")
    private Long netIncomeLoss;

    @Column(name = "net_income_loss_unit", length = 20)
    private String netIncomeLossUnit;

    @Column(name = "net_premiums_written")
    private Long netPremiumsWritten;

    @Column(name = "net_premiums_written_unit", length = 20)
    private String netPremiumsWrittenUnit;

    @Column(name = "total_assets")
    private Long totalAssets;

    @Column(name = "total_assets_unit", length = 20)
    private String totalAssetsUnit;

    @Column(name = "net_assets")
    private Long netAssets;

    @Column(name = "net_assets_unit", length = 20)
    private String netAssetsUnit;

    @Column(name = "capital_stock")
    private Long capitalStock;

    @Column(name = "capital_stock_unit", length = 20)
    private String capitalStockUnit;

    @Column(name = "number_of_employees")
    private Integer numberOfEmployees;

    @Column(name = "number_of_employees_unit", length = 20)
    private String numberOfEmployeesUnit;

    // ── Finance レベル情報 ─────────────────────────────────────────────────────

    @Column(name = "fiscal_year_cover_page", length = 200)
    private String fiscalYearCoverPage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "major_shareholders")
    private String majorShareholders;

    @Column(name = "finance_key_field", length = 200)
    private String financeKeyField;

    // ── 監査情報 ────────────────────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
