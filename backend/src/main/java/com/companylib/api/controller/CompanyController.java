package com.companylib.api.controller;

import com.companylib.api.domain.dto.*;
import com.companylib.api.service.CompanyNotFoundException;
import com.companylib.api.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<PageResponse<CompanySummaryDto>> search(
        @ModelAttribute CompanySearchRequest request
    ) {
        return ResponseEntity.ok(companyService.search(request));
    }

    @GetMapping("/{corporateNumber}")
    public ResponseEntity<CompanyDetailDto> getDetail(
        @PathVariable String corporateNumber
    ) {
        return ResponseEntity.ok(companyService.getDetail(corporateNumber));
    }
}
