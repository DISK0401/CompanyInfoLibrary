package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyCertification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Schema(description = "届出・認定情報")
@Getter
public class CertificationDto {

    @Schema(description = "認定名称", example = "くるみん認定")
    private final String title;

    @Schema(description = "認定カテゴリ", example = "子育てサポート企業")
    private final String category;

    @Schema(description = "認定日", example = "2022-11-01")
    private final LocalDate dateOfApproval;

    @Schema(description = "有効期限")
    private final LocalDate expireDate;

    @Schema(description = "認定府省", example = "厚生労働省")
    private final String governmentDepartments;

    @Schema(description = "対象")
    private final String target;

    public CertificationDto(CompanyCertification entity) {
        this.title = entity.getTitle();
        this.category = entity.getCategory();
        this.dateOfApproval = entity.getDateOfApproval();
        this.expireDate = entity.getExpireDate();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.target = entity.getTarget();
    }
}
