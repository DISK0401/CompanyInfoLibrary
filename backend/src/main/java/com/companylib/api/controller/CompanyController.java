package com.companylib.api.controller;

import com.companylib.api.domain.dto.*;
import com.companylib.api.domain.dto.search.AdvancedSearchRequest;
import com.companylib.api.service.CompanyRelatedInfoService;
import com.companylib.api.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "企業情報", description = "企業情報の検索・取得API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyRelatedInfoService companyRelatedInfoService;

    @Operation(summary = "企業簡易検索", description = "法人名・所在地・資本金・従業員数でフィルタリングして企業一覧を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "検索成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse")))
    })
    @GetMapping
    public ResponseEntity<PageResponse<CompanySummaryDto>> search(
        @Valid @ModelAttribute CompanySearchRequest request
    ) {
        return ResponseEntity.ok(companyService.search(request));
    }

    @Operation(summary = "企業高度検索", description = "複数条件・論理演算・ソートを指定して企業一覧を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "検索成功"),
        @ApiResponse(responseCode = "400", description = "バリデーションエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse")))
    })
    @PostMapping("/search")
    public ResponseEntity<PageResponse<CompanySummaryDto>> advancedSearch(
        @RequestBody @Valid AdvancedSearchRequest request
    ) {
        return ResponseEntity.ok(companyService.advancedSearch(request));
    }

    @Operation(summary = "企業詳細取得", description = "法人番号を指定して企業の詳細情報（基本情報・財務情報・職場情報・営業品目）を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}")
    public ResponseEntity<CompanyDetailDto> getDetail(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyService.getDetail(corporateNumber));
    }

    @Operation(summary = "補助金情報取得", description = "法人番号を指定して補助金・助成金の受給履歴を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/subsidies")
    public ResponseEntity<List<SubsidyDto>> getSubsidies(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getSubsidies(corporateNumber));
    }

    @Operation(summary = "特許情報取得", description = "法人番号を指定して特許・実用新案の取得履歴を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/patents")
    public ResponseEntity<List<PatentDto>> getPatents(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getPatents(corporateNumber));
    }

    @Operation(summary = "調達情報取得", description = "法人番号を指定して政府調達契約の履歴を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/procurements")
    public ResponseEntity<List<ProcurementDto>> getProcurements(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getProcurements(corporateNumber));
    }

    @Operation(summary = "届出・認定情報取得", description = "法人番号を指定して各種認定・届出の取得状況を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/certifications")
    public ResponseEntity<List<CertificationDto>> getCertifications(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getCertifications(corporateNumber));
    }

    @Operation(summary = "表彰情報取得", description = "法人番号を指定して受賞・表彰の履歴を取得します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/commendations")
    public ResponseEntity<List<CommendationDto>> getCommendations(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getCommendations(corporateNumber));
    }

    @Operation(summary = "職場情報取得", description = "法人番号を指定して職場環境・採用情報を取得します。情報が存在しない場合は204を返します。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "取得成功"),
        @ApiResponse(responseCode = "204", description = "職場情報なし"),
        @ApiResponse(responseCode = "400", description = "法人番号のフォーマットエラー",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "企業が見つからない",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{corporateNumber}/workplaces")
    public ResponseEntity<WorkplaceDto> getWorkplace(
        @Parameter(description = "法人番号（13桁の数字）", example = "1234567890123")
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        Optional<WorkplaceDto> workplace = companyRelatedInfoService.getWorkplace(corporateNumber);
        return workplace.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
