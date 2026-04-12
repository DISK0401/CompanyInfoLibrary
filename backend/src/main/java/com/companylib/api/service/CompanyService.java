package com.companylib.api.service;

import com.companylib.api.domain.dto.*;
import com.companylib.api.domain.entity.Company;
import com.companylib.api.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public PageResponse<CompanySummaryDto> search(CompanySearchRequest req) {
        PageRequest pageable = PageRequest.of(
            req.getPage(), req.getSize(),
            Sort.by(Sort.Direction.DESC, "capitalStock")
        );
        Page<CompanySummaryDto> result = companyRepository.searchCompanies(
            req.getName(),
            req.getLocation(),
            req.getMinCapital(),
            req.getMinEmployees(),
            pageable
        ).map(CompanySummaryDto::new);
        log.debug("Company search: name={}, location={}, results={}", req.getName(), req.getLocation(), result.getTotalElements());
        return new PageResponse<>(result);
    }

    @Transactional(readOnly = true)
    public CompanyDetailDto getDetail(String corporateNumber) {
        log.debug("Fetching company detail: corporateNumber={}", corporateNumber);
        Company company = companyRepository.findByIdWithDetails(corporateNumber)
            .orElseThrow(() -> {
                log.warn("Company not found: corporateNumber={}", corporateNumber);
                return new CompanyNotFoundException(corporateNumber);
            });
        return new CompanyDetailDto(company);
    }
}
