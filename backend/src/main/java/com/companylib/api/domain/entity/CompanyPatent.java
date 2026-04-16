package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_patents")
@Getter
@Setter
@NoArgsConstructor
public class CompanyPatent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_number", nullable = false)
    private Company company;

    @Column(name = "patent_type", length = 100)
    private String patentType;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "date_of_patent")
    private LocalDate dateOfPatent;

    @Column(name = "patent_number", length = 100)
    private String patentNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "classifications")
    private String classifications;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "key_field", length = 200)
    private String keyField;

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
