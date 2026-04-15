package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_procurements")
@Getter
@Setter
@NoArgsConstructor
public class CompanyProcurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_number", nullable = false)
    private Company company;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "date_of_order")
    private LocalDate dateOfOrder;

    @Column(name = "government_departments", length = 500)
    private String governmentDepartments;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

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
