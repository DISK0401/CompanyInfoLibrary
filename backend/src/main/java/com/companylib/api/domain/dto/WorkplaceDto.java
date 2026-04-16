package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyWorkplace;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class WorkplaceDto {
    private final BigDecimal averageAge;
    private final BigDecimal averageContinuousServiceYears;
    private final BigDecimal averageContinuousServiceYearsMale;
    private final BigDecimal averageContinuousServiceYearsFemale;
    private final String averageContinuousServiceYearsType;
    private final BigDecimal monthAverageOvertime;
    private final BigDecimal femaleWorkersProportion;
    private final String femaleWorkersProportionType;
    private final Integer femaleShareOfManager;
    private final Integer genderTotalOfManager;
    private final Integer femaleShareOfOfficers;
    private final Integer genderTotalOfOfficers;
    private final Integer paternityLeaveAcquisitionNum;
    private final Integer numberOfPaternityLeave;
    private final Integer maternityLeaveAcquisitionNum;
    private final Integer numberOfMaternityLeave;

    public WorkplaceDto(CompanyWorkplace entity) {
        this.averageAge = entity.getAverageAge();
        this.averageContinuousServiceYears = entity.getAverageContinuousServiceYears();
        this.averageContinuousServiceYearsMale = entity.getAverageContinuousServiceYearsMale();
        this.averageContinuousServiceYearsFemale = entity.getAverageContinuousServiceYearsFemale();
        this.averageContinuousServiceYearsType = entity.getAverageContinuousServiceYearsType();
        this.monthAverageOvertime = entity.getMonthAverageOvertime();
        this.femaleWorkersProportion = entity.getFemaleWorkersProportion();
        this.femaleWorkersProportionType = entity.getFemaleWorkersProportionType();
        this.femaleShareOfManager = entity.getFemaleShareOfManager();
        this.genderTotalOfManager = entity.getGenderTotalOfManager();
        this.femaleShareOfOfficers = entity.getFemaleShareOfOfficers();
        this.genderTotalOfOfficers = entity.getGenderTotalOfOfficers();
        this.paternityLeaveAcquisitionNum = entity.getPaternityLeaveAcquisitionNum();
        this.numberOfPaternityLeave = entity.getNumberOfPaternityLeave();
        this.maternityLeaveAcquisitionNum = entity.getMaternityLeaveAcquisitionNum();
        this.numberOfMaternityLeave = entity.getNumberOfMaternityLeave();
    }
}
