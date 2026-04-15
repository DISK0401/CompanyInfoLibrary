package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyCertificationRepository extends JpaRepository<CompanyCertification, Long> {

    List<CompanyCertification> findByCompanyCorporateNumberOrderByDateOfApprovalDesc(String corporateNumber);
}
