package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanySubsidy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Schema(description = "補助金情報")
@Getter
public class SubsidyDto {

    @Schema(description = "補助金名称", example = "IT導入補助金")
    private final String title;

    @Schema(description = "交付金額（円）", example = "4500000")
    private final Long amount;

    @Schema(description = "認定日", example = "2023-06-01")
    private final LocalDate dateOfApproval;

    @Schema(description = "担当府省", example = "経済産業省")
    private final String governmentDepartments;

    @Schema(description = "補助金財源")
    private final String subsidyResource;

    @Schema(description = "対象")
    private final String target;

    @Schema(description = "備考")
    private final String note;

    public SubsidyDto(CompanySubsidy entity) {
        this.title = entity.getTitle();
        this.amount = entity.getAmount();
        this.dateOfApproval = entity.getDateOfApproval();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.subsidyResource = entity.getSubsidyResource();
        this.target = entity.getTarget();
        this.note = entity.getNote();
    }
}
