package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyCommendation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Schema(description = "表彰情報")
@Getter
public class CommendationDto {

    @Schema(description = "表彰名称", example = "グッドデザイン賞")
    private final String title;

    @Schema(description = "表彰日", example = "2023-10-20")
    private final LocalDate dateOfCommendation;

    @Schema(description = "表彰府省・機関", example = "経済産業省")
    private final String governmentDepartments;

    @Schema(description = "表彰カテゴリ")
    private final String category;

    @Schema(description = "備考")
    private final String note;

    @Schema(description = "対象")
    private final String target;

    public CommendationDto(CompanyCommendation entity) {
        this.title = entity.getTitle();
        this.dateOfCommendation = entity.getDateOfCommendation();
        this.governmentDepartments = entity.getGovernmentDepartments();
        this.category = entity.getCategory();
        this.note = entity.getNote();
        this.target = entity.getTarget();
    }
}
