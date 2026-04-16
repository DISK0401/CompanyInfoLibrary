package com.companylib.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "企業簡易検索リクエスト")
@Getter
@Setter
public class CompanySearchRequest {

    @Schema(description = "法人名（部分一致）", example = "株式会社サンプル", maxLength = 200)
    @Size(max = 200)
    private String name;

    @Schema(description = "所在地（部分一致）", example = "東京都", maxLength = 200)
    @Size(max = 200)
    private String location;

    @Schema(description = "資本金の下限（円）", example = "10000000")
    @Min(0)
    private Long minCapital;

    @Schema(description = "従業員数の下限", example = "50")
    @Min(0)
    private Integer minEmployees;

    @Schema(description = "ページ番号（0始まり）", example = "0", defaultValue = "0")
    @Min(0)
    private int page = 0;

    @Schema(description = "1ページあたりの件数（1〜100）", example = "20", defaultValue = "20")
    @Min(1)
    @Max(100)
    private int size = 20;
}
