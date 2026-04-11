package com.companylib.api.repository;

import com.companylib.api.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, String> {

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        SELECT c FROM Company c
        WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:location IS NULL OR c.location LIKE CONCAT('%', :location, '%'))
          AND (:minCapital IS NULL OR c.capitalStock >= :minCapital)
          AND (:minEmployees IS NULL OR c.employeeNumber >= :minEmployees)
        """)
    Page<Company> searchCompanies(
        @Param("name") String name,
        @Param("location") String location,
        @Param("minCapital") Long minCapital,
        @Param("minEmployees") Integer minEmployees,
        Pageable pageable
    );
}
