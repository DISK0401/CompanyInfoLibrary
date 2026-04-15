package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanySubsidy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanySubsidyRepository extends JpaRepository<CompanySubsidy, Long> {

    List<CompanySubsidy> findByCompanyCorporateNumberOrderByDateOfApprovalDesc(String corporateNumber);
}
