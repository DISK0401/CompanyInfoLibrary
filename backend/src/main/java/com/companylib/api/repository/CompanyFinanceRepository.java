package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyFinanceRepository extends JpaRepository<CompanyFinance, Long> {
    List<CompanyFinance> findByCompanyCorporateNumberOrderByPeriodDesc(String corporateNumber);
}
