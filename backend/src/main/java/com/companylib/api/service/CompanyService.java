package com.companylib.api.service;

import com.companylib.api.domain.dto.*;
import com.companylib.api.domain.entity.Company;
import com.companylib.api.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

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
        return new PageResponse<>(result);
    }

    public CompanyDetailDto getDetail(String corporateNumber) {
        Company company = companyRepository.findById(corporateNumber)
            .orElseThrow(() -> new CompanyNotFoundException(corporateNumber));
        return new CompanyDetailDto(company);
    }
}
