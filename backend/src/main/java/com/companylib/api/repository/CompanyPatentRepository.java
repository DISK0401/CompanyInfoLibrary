package com.companylib.api.repository;

import com.companylib.api.domain.entity.CompanyPatent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompanyPatentRepository extends JpaRepository<CompanyPatent, Long> {

    List<CompanyPatent> findByCompanyCorporateNumberOrderByDateOfPatentDesc(String corporateNumber);
}
