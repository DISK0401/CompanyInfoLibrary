package com.companylib.api.service;

import com.companylib.api.domain.dto.*;
import com.companylib.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyRelatedInfoService {

    private final CompanyRepository companyRepository;
    private final CompanySubsidyRepository subsidyRepository;
    private final CompanyPatentRepository patentRepository;
    private final CompanyProcurementRepository procurementRepository;
    private final CompanyCertificationRepository certificationRepository;
    private final CompanyCommendationRepository commendationRepository;
    private final CompanyWorkplaceRepository workplaceRepository;

    private void verifyCompanyExists(String corporateNumber) {
        if (!companyRepository.existsById(corporateNumber)) {
            throw new CompanyNotFoundException(corporateNumber);
        }
    }

    public List<SubsidyDto> getSubsidies(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return subsidyRepository.findByCompanyCorporateNumberOrderByDateOfApprovalDesc(corporateNumber)
                .stream()
                .map(SubsidyDto::new)
                .toList();
    }

    public List<PatentDto> getPatents(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return patentRepository.findByCompanyCorporateNumberOrderByDateOfPatentDesc(corporateNumber)
                .stream()
                .map(PatentDto::new)
                .toList();
    }

    public List<ProcurementDto> getProcurements(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return procurementRepository.findByCompanyCorporateNumberOrderByDateOfOrderDesc(corporateNumber)
                .stream()
                .map(ProcurementDto::new)
                .toList();
    }

    public List<CertificationDto> getCertifications(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return certificationRepository.findByCompanyCorporateNumberOrderByDateOfApprovalDesc(corporateNumber)
                .stream()
                .map(CertificationDto::new)
                .toList();
    }

    public List<CommendationDto> getCommendations(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return commendationRepository.findByCompanyCorporateNumberOrderByDateOfCommendationDesc(corporateNumber)
                .stream()
                .map(CommendationDto::new)
                .toList();
    }

    public Optional<WorkplaceDto> getWorkplace(String corporateNumber) {
        verifyCompanyExists(corporateNumber);
        return workplaceRepository.findByCompanyCorporateNumber(corporateNumber)
                .map(WorkplaceDto::new);
    }
}
