package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyProcurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyProcurementRepository extends JpaRepository<CompanyProcurement, Long> {

    List<CompanyProcurement> findByCompanyCorporateNumberOrderByDateOfOrderDesc(String corporateNumber);
}
