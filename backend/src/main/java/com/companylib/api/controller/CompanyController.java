package com.companylib.api.controller;

import com.companylib.api.domain.dto.*;
import com.companylib.api.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<PageResponse<CompanySummaryDto>> search(
        @Valid @ModelAttribute CompanySearchRequest request
    ) {
        return ResponseEntity.ok(companyService.search(request));
    }

    @GetMapping("/{corporateNumber}")
    public ResponseEntity<CompanyDetailDto> getDetail(
        @PathVariable @Pattern(regexp = "\\d{13}", message = "法人番号は13桁の数字で指定してください") String corporateNumber
    ) {
        return ResponseEntity.ok(companyService.getDetail(corporateNumber));
    }
}
