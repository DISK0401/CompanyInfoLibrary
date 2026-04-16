package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyWorkplace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.math.BigDecimal;

@Schema(description = "職場情報")
@Getter
public class WorkplaceDto {

    @Schema(description = "平均年齢（歳）", example = "38.5")
    private final BigDecimal averageAge;

    @Schema(description = "平均継続勤務年数（年）", example = "12.3")
    private final BigDecimal averageContinuousServiceYears;

    @Schema(description = "男性平均継続勤務年数（年）", example = "13.5")
    private final BigDecimal averageContinuousServiceYearsMale;

    @Schema(description = "女性平均継続勤務年数（年）", example = "10.2")
    private final BigDecimal averageContinuousServiceYearsFemale;

    @Schema(description = "平均継続勤務年数の区分", example = "正規雇用労働者")
    private final String averageContinuousServiceYearsType;

    @Schema(description = "月平均残業時間（時間）", example = "18.0")
    private final BigDecimal monthAverageOvertime;

    @Schema(description = "女性労働者の割合（%）", example = "42.5")
    private final BigDecimal femaleWorkersProportion;

    @Schema(description = "女性労働者割合の区分", example = "正規雇用労働者")
    private final String femaleWorkersProportionType;

    @Schema(description = "管理職に占める女性の人数", example = "15")
    private final Integer femaleShareOfManager;

    @Schema(description = "管理職の総人数", example = "50")
    private final Integer genderTotalOfManager;

    @Schema(description = "役員に占める女性の人数", example = "3")
    private final Integer femaleShareOfOfficers;

    @Schema(description = "役員の総人数", example = "10")
    private final Integer genderTotalOfOfficers;

    @Schema(description = "育児休業取得者数（男性）", example = "8")
    private final Integer paternityLeaveAcquisitionNum;

    @Schema(description = "育児休業対象者数（男性）", example = "20")
    private final Integer numberOfPaternityLeave;

    @Schema(description = "育児休業取得者数（女性）", example = "12")
    private final Integer maternityLeaveAcquisitionNum;

    @Schema(description = "育児休業対象者数（女性）", example = "15")
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
