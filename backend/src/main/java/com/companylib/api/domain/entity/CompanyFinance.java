package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(name = "net_sales")
    private Long netSales;

    @Column(name = "gross_operating_revenue")
    private Long grossOperatingRevenue;

    @Column(name = "operating_revenue1")
    private Long operatingRevenue1;

    @Column(name = "operating_revenue2")
    private Long operatingRevenue2;

    @Column(name = "net_income_loss")
    private Long netIncomeLoss;

    @Column(name = "ordinary_income_loss")
    private Long ordinaryIncomeLoss;

    @Column(name = "total_assets")
    private Long totalAssets;

    @Column(name = "net_assets")
    private Long netAssets;

    @Column(name = "capital_stock")
    private Long capitalStock;

    @Column(name = "number_of_employees")
    private Integer numberOfEmployees;

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
