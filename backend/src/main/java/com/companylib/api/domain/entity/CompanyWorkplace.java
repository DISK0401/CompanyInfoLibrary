package com.companylib.api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_workplaces")
@Getter
@Setter
@NoArgsConstructor
public class CompanyWorkplace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_number", nullable = false, unique = true)
    private Company company;

    // 勤務基本情報
    @Column(name = "average_age", precision = 5, scale = 1)
    private BigDecimal averageAge;

    @Column(name = "average_continuous_service_years", precision = 5, scale = 1)
    private BigDecimal averageContinuousServiceYears;

    @Column(name = "average_continuous_service_years_male", precision = 5, scale = 1)
    private BigDecimal averageContinuousServiceYearsMale;

    @Column(name = "average_continuous_service_years_female", precision = 5, scale = 1)
    private BigDecimal averageContinuousServiceYearsFemale;

    @Column(name = "average_continuous_service_years_type", length = 50)
    private String averageContinuousServiceYearsType;

    @Column(name = "month_average_overtime", precision = 5, scale = 1)
    private BigDecimal monthAverageOvertime;

    // 女性活躍情報
    @Column(name = "female_workers_proportion", precision = 6, scale = 2)
    private BigDecimal femaleWorkersProportion;

    @Column(name = "female_workers_proportion_type", length = 50)
    private String femaleWorkersProportionType;

    @Column(name = "female_share_of_manager")
    private Integer femaleShareOfManager;

    @Column(name = "gender_total_of_manager")
    private Integer genderTotalOfManager;

    @Column(name = "female_share_of_officers")
    private Integer femaleShareOfOfficers;

    @Column(name = "gender_total_of_officers")
    private Integer genderTotalOfOfficers;

    // 育児・仕事両立
    @Column(name = "paternity_leave_acquisition_num")
    private Integer paternityLeaveAcquisitionNum;

    @Column(name = "number_of_paternity_leave")
    private Integer numberOfPaternityLeave;

    @Column(name = "maternity_leave_acquisition_num")
    private Integer maternityLeaveAcquisitionNum;

    @Column(name = "number_of_maternity_leave")
    private Integer numberOfMaternityLeave;

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
