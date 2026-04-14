package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "kind", length = 10)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "industry")
    private String industry;

    @Column(name = "qualification_grade", length = 200)
    private String qualificationGrade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data_json")
    private String metaDataJson;

    @Column(name = "process", length = 10)
    private String process;

    @Column(name = "aggregated_year", length = 10)
    private String aggregatedYear;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CompanyFinance> finances = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CompanyBusinessItem> businessItems = new HashSet<>();

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
