package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @Column(name = "corporate_number", length = 13, nullable = false)
    private String corporateNumber;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "kana", length = 500)
    private String kana;

    @Column(name = "name_en", length = 500)
    private String nameEn;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "company_url", columnDefinition = "TEXT")
    private String companyUrl;

    @Column(name = "business_summary", columnDefinition = "TEXT")
    private String businessSummary;

    @Column(name = "capital_stock")
    private Long capitalStock;

    @Column(name = "employee_number")
    private Integer employeeNumber;

    @Column(name = "company_size_male")
    private Integer companySizeMale;

    @Column(name = "company_size_female")
    private Integer companySizeFemale;

    @Column(name = "representative_name", length = 200)
    private String representativeName;

    @Column(name = "representative_position", length = 200)
    private String representativePosition;

    @Column(name = "date_of_establishment")
    private LocalDate dateOfEstablishment;

    @Column(name = "founding_year")
    private Integer foundingYear;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "close_cause", length = 200)
    private String closeCause;

    @Column(name = "gbizinfo_update_date")
    private LocalDate gbizinfoUpdateDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyFinance> finances = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompanyBusinessItem> businessItems = new ArrayList<>();

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
