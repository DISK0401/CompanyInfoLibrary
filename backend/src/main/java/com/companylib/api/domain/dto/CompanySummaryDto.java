package com.companylib.api.domain.dto;

import com.companylib.api.domain.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "企業サマリー情報（検索結果1件）")
@Getter
public class CompanySummaryDto {

    @Schema(description = "法人番号（13桁）", example = "1234567890123")
    private final String corporateNumber;

    @Schema(description = "法人名", example = "株式会社サンプル")
    private final String name;

    @Schema(description = "本社所在地", example = "東京都千代田区丸の内1-1-1")
    private final String location;

    @Schema(description = "資本金（円）", example = "100000000")
    private final Long capitalStock;

    @Schema(description = "従業員数", example = "500")
    private final Integer employeeNumber;

    @Schema(description = "代表者名", example = "山田 太郎")
    private final String representativeName;

    @Schema(description = "創業年", example = "1990")
    private final Integer foundingYear;

    public CompanySummaryDto(Company c) {
        this.corporateNumber = c.getCorporateNumber();
        this.name = c.getName();
        this.location = c.getLocation();
        this.capitalStock = c.getCapitalStock();
        this.employeeNumber = c.getEmployeeNumber();
        this.representativeName = c.getRepresentativeName();
        this.foundingYear = c.getFoundingYear();
    }
}
