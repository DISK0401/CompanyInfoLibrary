package com.companylib.api.controller;

import com.companylib.api.domain.dto.*;
import com.companylib.api.domain.dto.search.AdvancedSearchRequest;
import com.companylib.api.service.CompanyRelatedInfoService;
import com.companylib.api.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyRelatedInfoService companyRelatedInfoService;

    /** 既存の簡易検索（後方互換） */
    @GetMapping
    public ResponseEntity<PageResponse<CompanySummaryDto>> search(
        @Valid @ModelAttribute CompanySearchRequest request
    ) {
        return ResponseEntity.ok(companyService.search(request));
    }

    /** 高度検索（クエリビルダー対応） */
    @PostMapping("/search")
    public ResponseEntity<PageResponse<CompanySummaryDto>> advancedSearch(
        @RequestBody @Valid AdvancedSearchRequest request
    ) {
        return ResponseEntity.ok(companyService.advancedSearch(request));
    }

    /** 企業詳細取得 */
    @GetMapping("/{corporateNumber}")
    public ResponseEntity<CompanyDetailDto> getDetail(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyService.getDetail(corporateNumber));
    }

    /** 補助金情報 */
    @GetMapping("/{corporateNumber}/subsidies")
    public ResponseEntity<List<SubsidyDto>> getSubsidies(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getSubsidies(corporateNumber));
    }

    /** 特許情報 */
    @GetMapping("/{corporateNumber}/patents")
    public ResponseEntity<List<PatentDto>> getPatents(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getPatents(corporateNumber));
    }

    /** 調達情報 */
    @GetMapping("/{corporateNumber}/procurements")
    public ResponseEntity<List<ProcurementDto>> getProcurements(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getProcurements(corporateNumber));
    }

    /** 届出・認定情報 */
    @GetMapping("/{corporateNumber}/certifications")
    public ResponseEntity<List<CertificationDto>> getCertifications(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getCertifications(corporateNumber));
    }

    /** 表彰情報 */
    @GetMapping("/{corporateNumber}/commendations")
    public ResponseEntity<List<CommendationDto>> getCommendations(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyRelatedInfoService.getCommendations(corporateNumber));
    }

    /** 職場情報 */
    @GetMapping("/{corporateNumber}/workplaces")
    public ResponseEntity<WorkplaceDto> getWorkplace(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        Optional<WorkplaceDto> workplace = companyRelatedInfoService.getWorkplace(corporateNumber);
        return workplace.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
