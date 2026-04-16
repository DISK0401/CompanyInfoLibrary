package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.CompanyPatent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.time.LocalDate;

@Schema(description = "特許情報")
@Getter
public class PatentDto {

    @Schema(description = "特許種別（特許・実用新案等）", example = "特許")
    private final String patentType;

    @Schema(description = "発明の名称", example = "情報処理方法および情報処理装置")
    private final String title;

    @Schema(description = "登録日", example = "2022-03-15")
    private final LocalDate dateOfPatent;

    @Schema(description = "特許番号", example = "特許第7000000号")
    private final String patentNumber;

    @Schema(description = "IPC分類コード", example = "G06F 16/00")
    private final String classifications;

    @Schema(description = "J-PlatPat等の参照URL")
    private final String url;

    public PatentDto(CompanyPatent entity) {
        this.patentType = entity.getPatentType();
        this.title = entity.getTitle();
        this.dateOfPatent = entity.getDateOfPatent();
        this.patentNumber = entity.getPatentNumber();
        this.classifications = entity.getClassifications();
        this.url = entity.getUrl();
    }
}
