package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyCommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyCommendationRepository extends JpaRepository<CompanyCommendation, Long> {

    List<CompanyCommendation> findByCompanyCorporateNumberOrderByDateOfCommendationDesc(String corporateNumber);
}
