package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyUpsertService {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_COMPANY = """
        INSERT INTO companies (
            corporate_number, name, kana, name_en, postal_code, location,
            company_url, business_summary, capital_stock, employee_number,
            company_size_male, company_size_female,
            representative_name, representative_position,
            date_of_establishment, founding_year, status,
            close_date, close_cause, gbizinfo_update_date,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (corporate_number) DO UPDATE SET
            name = EXCLUDED.name,
            kana = EXCLUDED.kana,
            name_en = EXCLUDED.name_en,
            postal_code = EXCLUDED.postal_code,
            location = EXCLUDED.location,
            company_url = EXCLUDED.company_url,
            business_summary = EXCLUDED.business_summary,
            capital_stock = EXCLUDED.capital_stock,
            employee_number = EXCLUDED.employee_number,
            company_size_male = EXCLUDED.company_size_male,
            company_size_female = EXCLUDED.company_size_female,
            representative_name = EXCLUDED.representative_name,
            representative_position = EXCLUDED.representative_position,
            date_of_establishment = EXCLUDED.date_of_establishment,
            founding_year = EXCLUDED.founding_year,
            status = EXCLUDED.status,
            close_date = EXCLUDED.close_date,
            close_cause = EXCLUDED.close_cause,
            gbizinfo_update_date = EXCLUDED.gbizinfo_update_date,
            updated_at = NOW()
        """;

    @Transactional
    public void upsert(HojinInfo info) {
        jdbcTemplate.update(UPSERT_COMPANY,
            info.getCorporateNumber(),
            info.getName(),
            info.getKana(),
            info.getNameEn(),
            info.getPostalCode(),
            info.getLocation(),
            info.getCompanyUrl(),
            info.getBusinessSummary(),
            info.getCapitalStock(),
            info.getEmployeeNumber(),
            info.getCompanySizeMale(),
            info.getCompanySizeFemale(),
            info.getRepresentativeName(),
            info.getRepresentativePosition(),
            parseDate(info.getDateOfEstablishment()),
            info.getFoundingYear(),
            info.getStatus(),
            parseDate(info.getCloseDate()),
            info.getCloseCause(),
            parseDate(info.getUpdateDate())
        );
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return Date.valueOf(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e) {
            log.warn("日付パース失敗: {}", dateStr);
            return null;
        }
    }
}
