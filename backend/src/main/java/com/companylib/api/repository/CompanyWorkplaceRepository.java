package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyWorkplace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyWorkplaceRepository extends JpaRepository<CompanyWorkplace, Long> {

    Optional<CompanyWorkplace> findByCompanyCorporateNumber(String corporateNumber);
}
