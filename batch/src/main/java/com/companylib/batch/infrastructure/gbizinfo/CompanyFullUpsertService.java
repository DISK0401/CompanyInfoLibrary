package com.companylib.batch.infrastructure.gbizinfo;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.companylib.batch.infrastructure.gbizinfo.dto.FinancialStatement;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import com.companylib.batch.infrastructure.gbizinfo.dto.KihonjohoRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * gBizINFO データを全関連テーブルへ UPSERT するサービス。
 * HojinInfo（Hojinjoho JSON / API レスポンス）から企業に紐づく全テーブルを一括更新する。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyFullUpsertService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // ── SQL 定数 ─────────────────────────────────────────────────────────────

    private static final String UPSERT_COMPANY = """
        INSERT INTO companies (
            corporate_number, name, kana, name_en, postal_code, location,
            company_url, business_summary, capital_stock, employee_number,
            company_size_male, company_size_female,
            representative_name,
            date_of_establishment, founding_year, status,
            close_date, close_cause, gbizinfo_update_date,
            kind, industry, qualification_grade, meta_data_json,
            process, aggregated_year,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb,?,?::jsonb,?,?,NOW(),NOW())
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
            date_of_establishment = EXCLUDED.date_of_establishment,
            founding_year = EXCLUDED.founding_year,
            status = EXCLUDED.status,
            close_date = EXCLUDED.close_date,
            close_cause = EXCLUDED.close_cause,
            gbizinfo_update_date = EXCLUDED.gbizinfo_update_date,
            kind = EXCLUDED.kind,
            industry = EXCLUDED.industry,
            qualification_grade = EXCLUDED.qualification_grade,
            meta_data_json = EXCLUDED.meta_data_json,
            process = EXCLUDED.process,
            aggregated_year = EXCLUDED.aggregated_year,
            updated_at = NOW()
        """;

    private static final String DELETE_BUSINESS_ITEMS =
        "DELETE FROM company_business_items WHERE corporate_number = ?";

    private static final String INSERT_BUSINESS_ITEM =
        "INSERT INTO company_business_items (corporate_number, item_name) VALUES (?, ?)"
        + " ON CONFLICT (corporate_number, item_name) DO NOTHING";

    private static final String UPSERT_FINANCE = """
        INSERT INTO company_finances (
            corporate_number, period, accounting_standards,
            net_sales, net_sales_unit,
            gross_operating_revenue, gross_operating_revenue_unit,
            operating_revenue1, operating_revenue1_unit,
            operating_revenue2, operating_revenue2_unit,
            ordinary_income, ordinary_income_unit,
            ordinary_income_loss, ordinary_income_loss_unit,
            net_income_loss, net_income_loss_unit,
            net_premiums_written, net_premiums_written_unit,
            total_assets, total_assets_unit,
            net_assets, net_assets_unit,
            capital_stock, capital_stock_unit,
            number_of_employees, number_of_employees_unit,
            fiscal_year_cover_page, major_shareholders, finance_key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb,?,NOW(),NOW())
        ON CONFLICT (corporate_number, period) DO UPDATE SET
            accounting_standards = EXCLUDED.accounting_standards,
            net_sales = EXCLUDED.net_sales,
            net_sales_unit = EXCLUDED.net_sales_unit,
            gross_operating_revenue = EXCLUDED.gross_operating_revenue,
            gross_operating_revenue_unit = EXCLUDED.gross_operating_revenue_unit,
            operating_revenue1 = EXCLUDED.operating_revenue1,
            operating_revenue1_unit = EXCLUDED.operating_revenue1_unit,
            operating_revenue2 = EXCLUDED.operating_revenue2,
            operating_revenue2_unit = EXCLUDED.operating_revenue2_unit,
            ordinary_income = EXCLUDED.ordinary_income,
            ordinary_income_unit = EXCLUDED.ordinary_income_unit,
            ordinary_income_loss = EXCLUDED.ordinary_income_loss,
            ordinary_income_loss_unit = EXCLUDED.ordinary_income_loss_unit,
            net_income_loss = EXCLUDED.net_income_loss,
            net_income_loss_unit = EXCLUDED.net_income_loss_unit,
            net_premiums_written = EXCLUDED.net_premiums_written,
            net_premiums_written_unit = EXCLUDED.net_premiums_written_unit,
            total_assets = EXCLUDED.total_assets,
            total_assets_unit = EXCLUDED.total_assets_unit,
            net_assets = EXCLUDED.net_assets,
            net_assets_unit = EXCLUDED.net_assets_unit,
            capital_stock = EXCLUDED.capital_stock,
            capital_stock_unit = EXCLUDED.capital_stock_unit,
            number_of_employees = EXCLUDED.number_of_employees,
            number_of_employees_unit = EXCLUDED.number_of_employees_unit,
            fiscal_year_cover_page = EXCLUDED.fiscal_year_cover_page,
            major_shareholders = EXCLUDED.major_shareholders,
            finance_key_field = EXCLUDED.finance_key_field,
            updated_at = NOW()
        """;

    private static final String UPSERT_SUBSIDY = """
        INSERT INTO company_subsidies (
            corporate_number, title, amount, date_of_approval,
            government_departments, target, key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            title = EXCLUDED.title,
            amount = EXCLUDED.amount,
            date_of_approval = EXCLUDED.date_of_approval,
            government_departments = EXCLUDED.government_departments,
            target = EXCLUDED.target,
            updated_at = NOW()
        """;

    private static final String UPSERT_PATENT = """
        INSERT INTO company_patents (
            corporate_number, patent_type, title, patent_number, date_of_patent,
            classifications, url, key_field, created_at, updated_at
        ) VALUES (?,?,?,?,?,?::jsonb,?,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            patent_type = EXCLUDED.patent_type,
            title = EXCLUDED.title,
            patent_number = EXCLUDED.patent_number,
            date_of_patent = EXCLUDED.date_of_patent,
            classifications = EXCLUDED.classifications,
            url = EXCLUDED.url,
            updated_at = NOW()
        """;

    private static final String UPSERT_PROCUREMENT = """
        INSERT INTO company_procurements (
            corporate_number, title, amount, date_of_order,
            government_departments, note, key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            title = EXCLUDED.title,
            amount = EXCLUDED.amount,
            date_of_order = EXCLUDED.date_of_order,
            government_departments = EXCLUDED.government_departments,
            note = EXCLUDED.note,
            updated_at = NOW()
        """;

    private static final String UPSERT_CERTIFICATION = """
        INSERT INTO company_certifications (
            corporate_number, title, category, date_of_approval, expire_date,
            government_departments, target, key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            title = EXCLUDED.title,
            category = EXCLUDED.category,
            date_of_approval = EXCLUDED.date_of_approval,
            expire_date = EXCLUDED.expire_date,
            government_departments = EXCLUDED.government_departments,
            target = EXCLUDED.target,
            updated_at = NOW()
        """;

    private static final String UPSERT_COMMENDATION = """
        INSERT INTO company_commendations (
            corporate_number, title, date_of_commendation, government_departments,
            category, note, target, key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            title = EXCLUDED.title,
            date_of_commendation = EXCLUDED.date_of_commendation,
            government_departments = EXCLUDED.government_departments,
            category = EXCLUDED.category,
            note = EXCLUDED.note,
            target = EXCLUDED.target,
            updated_at = NOW()
        """;

    private static final String UPSERT_WORKPLACE = """
        INSERT INTO company_workplaces (
            corporate_number,
            average_age, average_continuous_service_years,
            average_continuous_service_years_male, average_continuous_service_years_female,
            average_continuous_service_years_type, month_average_overtime,
            female_workers_proportion, female_workers_proportion_type,
            female_share_of_manager, gender_total_of_manager,
            female_share_of_officers, gender_total_of_officers,
            paternity_leave_acquisition_num, number_of_paternity_leave,
            maternity_leave_acquisition_num, number_of_maternity_leave,
            key_field, created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (corporate_number) DO UPDATE SET
            average_age = EXCLUDED.average_age,
            average_continuous_service_years = EXCLUDED.average_continuous_service_years,
            average_continuous_service_years_male = EXCLUDED.average_continuous_service_years_male,
            average_continuous_service_years_female = EXCLUDED.average_continuous_service_years_female,
            average_continuous_service_years_type = EXCLUDED.average_continuous_service_years_type,
            month_average_overtime = EXCLUDED.month_average_overtime,
            female_workers_proportion = EXCLUDED.female_workers_proportion,
            female_workers_proportion_type = EXCLUDED.female_workers_proportion_type,
            female_share_of_manager = EXCLUDED.female_share_of_manager,
            gender_total_of_manager = EXCLUDED.gender_total_of_manager,
            female_share_of_officers = EXCLUDED.female_share_of_officers,
            gender_total_of_officers = EXCLUDED.gender_total_of_officers,
            paternity_leave_acquisition_num = EXCLUDED.paternity_leave_acquisition_num,
            number_of_paternity_leave = EXCLUDED.number_of_paternity_leave,
            maternity_leave_acquisition_num = EXCLUDED.maternity_leave_acquisition_num,
            number_of_maternity_leave = EXCLUDED.number_of_maternity_leave,
            key_field = EXCLUDED.key_field,
            updated_at = NOW()
        """;

    private static final String UPSERT_FINANCIAL_STATEMENT = """
        INSERT INTO company_financial_statements (
            corporate_number, period, release_date, unit, status,
            report_data, key_field,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?::jsonb,?,NOW(),NOW())
        ON CONFLICT (key_field) DO UPDATE SET
            corporate_number = EXCLUDED.corporate_number,
            period = EXCLUDED.period,
            release_date = EXCLUDED.release_date,
            unit = EXCLUDED.unit,
            status = EXCLUDED.status,
            report_data = EXCLUDED.report_data,
            updated_at = NOW()
        """;

    /**
     * Kihonjoho CSV → companies の INSERT ONLY。
     * Hojinjoho JSON で既に登録済みの法人はスキップし（DO NOTHING）、
     * 未登録の法人のみ基本情報を補完する。
     */
    private static final String INSERT_COMPANY_KIHONJOHO = """
        INSERT INTO companies (
            corporate_number, name, kana, name_en, postal_code, location,
            company_url, business_summary, capital_stock, employee_number,
            company_size_male, company_size_female,
            representative_name,
            date_of_establishment, founding_year, status,
            close_date, close_cause, gbizinfo_update_date,
            kind, qualification_grade, process,
            created_at, updated_at
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())
        ON CONFLICT (corporate_number) DO NOTHING
        """;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * HojinInfo（法人情報一括）から全関連テーブルを UPSERT する。
     */
    @Transactional
    public void upsert(HojinInfo info) {
        String cn = info.getCorporateNumber();
        log.debug("UPSERT 開始: corporateNumber={}", cn);

        upsertCompany(info);
        upsertBusinessItems(cn, info.getBusinessItems());
        upsertFinances(cn, info.getFinance());
        upsertSubsidies(cn, info.getSubsidies());
        upsertPatents(cn, info.getPatents());
        upsertProcurements(cn, info.getProcurements());
        upsertCertifications(cn, info.getCertifications());
        upsertCommendations(cn, info.getCommendations());
        upsertWorkplace(cn, info.getWorkplaceInfo());
    }

    /**
     * KihonjohoRow（法人基本情報 CSV）から companies・company_business_items テーブルを補完する。
     * Hojinjoho JSON の後に実行し、Hojinjoho に含まれない法人のみ基本情報を INSERT する。
     * 既存レコード（Hojinjoho で登録済み）はスキップ（ON CONFLICT DO NOTHING）。
     *
     * @return 新規挿入件数（0=既存レコードのためスキップ, 1=新規挿入）
     */
    @Transactional
    public int upsertFromKihonjoho(KihonjohoRow row) {
        int inserted = jdbcTemplate.update(INSERT_COMPANY_KIHONJOHO,
            row.getCorporateNumber(),
            row.getName(),
            row.getKana(),
            row.getNameEn(),
            row.getPostalCode(),
            row.getLocation(),
            row.getCompanyUrl(),
            row.getBusinessSummary(),
            parseLong(row.getCapitalStockStr()),
            parseInteger(row.getEmployeeNumberStr()),
            parseInteger(row.getCompanySizeMaleStr()),
            parseInteger(row.getCompanySizeFemaleStr()),
            row.getRepresentativeName(),
            parseDate(row.getDateOfEstablishmentStr()),
            parseInteger(row.getFoundingYearStr()),
            row.getStatus(),
            parseDate(row.getCloseDateStr()),
            row.getCloseCause(),
            parseDate(row.getUpdateDateStr()),
            row.getKind(),
            row.getQualificationGrade(),
            row.getProcess()
        );

        // 新規挿入された法人のみ business_items を登録
        if (inserted > 0) {
            upsertBusinessItems(row.getCorporateNumber(), row.getBusinessItems());
        }
        return inserted;
    }

    /**
     * FinancialStatement（Kessanjoho XML）を company_financial_statements テーブルに UPSERT する。
     *
     * <p>REQUIRES_NEW を使用して独立したトランザクションで実行することで、
     * FK 違反（companies に未登録の法人番号）が発生した場合に
     * Spring Batch のチャンクトランザクションを汚染せず、
     * ステップレベルのスキップ機構が正常に動作することを保証する。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertFinancialStatement(FinancialStatement fs) {
        if (fs.getCorporateNumber() == null || fs.getKeyField() == null) {
            log.warn("決算情報スキップ（必須フィールド欠損）: period={}", fs.getPeriod());
            return;
        }

        String reportDataJson;
        try {
            reportDataJson = objectMapper.writeValueAsString(fs.getReports());
        } catch (JsonProcessingException e) {
            log.warn("報告書データの JSON シリアライズ失敗: corporateNumber={}, period={}",
                fs.getCorporateNumber(), fs.getPeriod(), e);
            return;
        }

        jdbcTemplate.update(UPSERT_FINANCIAL_STATEMENT,
            fs.getCorporateNumber(),
            fs.getPeriod(),
            fs.getReleaseDate(),
            fs.getUnit(),
            fs.getStatus(),
            reportDataJson,
            fs.getKeyField()
        );
    }

    // ── Private UPSERT メソッド ────────────────────────────────────────────────

    private void upsertCompany(HojinInfo info) {
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
            parseDate(info.getDateOfEstablishment()),
            info.getFoundingYear(),
            info.getStatus(),
            parseDate(info.getCloseDate()),
            info.getCloseCause(),
            parseDate(info.getUpdateDate()),
            info.getKind(),
            toJson(info.getIndustry()),
            info.getQualificationGrade(),
            toJson(info.getCompanyMetadata()),
            info.getProcess(),
            info.getAggregatedYear()
        );
    }

    private void upsertBusinessItems(String cn, List<String> items) {
        if (items == null || items.isEmpty()) return;

        // 有効レコードを先に確定してから DELETE を実行（空フィルタ後に DELETE のみ走るのを防ぐ）
        List<Object[]> args = items.stream()
            .filter(item -> item != null && !item.isBlank())
            .map(item -> new Object[]{cn, item})
            .toList();

        if (args.isEmpty()) return;

        jdbcTemplate.update(DELETE_BUSINESS_ITEMS, cn);
        jdbcTemplate.batchUpdate(INSERT_BUSINESS_ITEM, args);
    }

    private void upsertFinances(String cn, HojinInfo.Finance finance) {
        if (finance == null || finance.getManagementIndex() == null) return;

        // accounting_standards と major_shareholders は Finance レベル（全期共通）
        String accountingStandards = finance.getAccountingStandards();
        String fiscalYearCoverPage = finance.getFiscalYearCoverPage();
        String majorShareholdersJson = toJson(finance.getMajorShareholders());

        String financeKeyField = finance.getMetadata() != null
            ? finance.getMetadata().getKeyField() : null;

        List<Object[]> args = finance.getManagementIndex().stream()
            .filter(m -> m.getPeriod() != null)
            .map(m -> new Object[]{
                cn,
                m.getPeriod(),
                accountingStandards,
                m.getNetSales(),               m.getNetSalesUnit(),
                m.getGrossOperatingRevenue(),  m.getGrossOperatingRevenueUnit(),
                m.getOperatingRevenue1(),      m.getOperatingRevenue1Unit(),
                m.getOperatingRevenue2(),      m.getOperatingRevenue2Unit(),
                m.getOrdinaryIncome(),         m.getOrdinaryIncomeUnit(),
                m.getOrdinaryIncomeLoss(),     m.getOrdinaryIncomeLossUnit(),
                m.getNetIncomeLoss(),          m.getNetIncomeLossUnit(),
                m.getNetPremiumsWritten(),     m.getNetPremiumsWrittenUnit(),
                m.getTotalAssets(),            m.getTotalAssetsUnit(),
                m.getNetAssets(),              m.getNetAssetsUnit(),
                m.getCapitalStock(),           m.getCapitalStockUnit(),
                m.getNumberOfEmployees(),      m.getNumberOfEmployeesUnit(),
                fiscalYearCoverPage,
                majorShareholdersJson,
                financeKeyField
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_FINANCE, args);
        }
    }

    private void upsertSubsidies(String cn, List<HojinInfo.SubsidyInfo> subsidies) {
        if (subsidies == null || subsidies.isEmpty()) return;

        List<Object[]> args = subsidies.stream()
            .filter(s -> keyField(s.getMetadata()) != null)
            .map(s -> new Object[]{
                cn,
                s.getTitle(),
                parseLong(s.getAmount()),
                parseDate(s.getDateOfApproval()),
                s.getGovernmentDepartments(),
                s.getTarget(),
                keyField(s.getMetadata())
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_SUBSIDY, args);
        }
    }

    private void upsertPatents(String cn, List<HojinInfo.PatentInfo> patents) {
        if (patents == null || patents.isEmpty()) return;

        List<Object[]> args = patents.stream()
            .filter(p -> keyField(p.getMetadata()) != null)
            .map(p -> new Object[]{
                cn,
                p.getPatentType(),
                p.getTitle(),
                p.getRegistrationNumber(),
                parseDate(p.getApplicationDate()),
                toJson(p.getClassifications()),
                p.getUrl(),
                keyField(p.getMetadata())
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_PATENT, args);
        }
    }

    private void upsertProcurements(String cn, List<HojinInfo.ProcurementInfo> procurements) {
        if (procurements == null || procurements.isEmpty()) return;

        List<Object[]> args = procurements.stream()
            .filter(p -> keyField(p.getMetadata()) != null)
            .map(p -> new Object[]{
                cn,
                p.getTitle(),
                p.getAmount(),
                parseDate(p.getDateOfOrder()),
                p.getGovernmentDepartments(),
                p.getNote(),
                keyField(p.getMetadata())
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_PROCUREMENT, args);
        }
    }

    private void upsertCertifications(String cn, List<HojinInfo.CertificationInfo> certifications) {
        if (certifications == null || certifications.isEmpty()) return;

        List<Object[]> args = certifications.stream()
            .filter(c -> keyField(c.getMetadata()) != null)
            .map(c -> new Object[]{
                cn,
                c.getTitle(),
                c.getCategory(),
                parseDate(c.getDateOfApproval()),
                parseDate(c.getExpireDate()),
                c.getGovernmentDepartments(),
                c.getTarget(),
                keyField(c.getMetadata())
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_CERTIFICATION, args);
        }
    }

    private void upsertCommendations(String cn, List<HojinInfo.CommendationInfo> commendations) {
        if (commendations == null || commendations.isEmpty()) return;

        List<Object[]> args = commendations.stream()
            .filter(c -> keyField(c.getMetadata()) != null)
            .map(c -> new Object[]{
                cn,
                c.getTitle(),
                parseDate(c.getDateOfCommendation()),
                c.getGovernmentDepartments(),
                c.getCategory(),
                c.getNote(),
                c.getTarget(),
                keyField(c.getMetadata())
            })
            .toList();

        if (!args.isEmpty()) {
            jdbcTemplate.batchUpdate(UPSERT_COMMENDATION, args);
        }
    }

    private void upsertWorkplace(String cn, HojinInfo.WorkplaceInfo workplace) {
        if (workplace == null) return;

        HojinInfo.WorkplaceBaseInfos base = workplace.getBaseInfos();
        HojinInfo.WomenActivityInfos women = workplace.getWomenActivityInfos();
        HojinInfo.CompatibilityOfChildcareAndWork childcare = workplace.getCompatibilityOfChildcareAndWork();

        jdbcTemplate.update(UPSERT_WORKPLACE,
            cn,
            base != null ? base.getAverageAge() : null,
            base != null ? base.getAverageContinuousServiceYears() : null,
            base != null ? base.getAverageContinuousServiceYearsMale() : null,
            base != null ? base.getAverageContinuousServiceYearsFemale() : null,
            base != null ? base.getAverageContinuousServiceYearsType() : null,
            base != null ? base.getMonthAverageOvertimeHours() : null,
            women != null ? women.getFemaleWorkersProportion() : null,
            women != null ? women.getFemaleWorkersProportionType() : null,
            women != null ? women.getFemaleShareOfManager() : null,
            women != null ? women.getGenderTotalOfManager() : null,
            women != null ? women.getFemaleShareOfOfficers() : null,
            women != null ? women.getGenderTotalOfOfficers() : null,
            childcare != null ? childcare.getPaternityLeaveAcquisitionNum() : null,
            childcare != null ? childcare.getNumberOfPaternityLeave() : null,
            childcare != null ? childcare.getMaternityLeaveAcquisitionNum() : null,
            childcare != null ? childcare.getNumberOfMaternityLeave() : null,
            keyField(workplace.getMetadata())
        );
    }

    // ── ユーティリティ ─────────────────────────────────────────────────────────

    /**
     * 日付文字列を java.sql.Date に変換する。
     * gBizINFO JSON は ISO 日付形式（yyyy-MM-dd）と
     * ISO オフセット日時形式（yyyy-MM-dd'T'HH:mm:ssXXX）の両方が存在するため、
     * どちらのフォーマットも受け付ける。
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            // まず ISO date（yyyy-MM-dd）で試みる
            return Date.valueOf(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e1) {
            try {
                // 次に ISO offset datetime（yyyy-MM-dd'T'HH:mm:ssXXX）で試みる
                return Date.valueOf(OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toLocalDate());
            } catch (DateTimeParseException e2) {
                log.debug("日付パース失敗（スキップ）: {}", dateStr);
                return null;
            }
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** DB の INTEGER カラム向けパース。parseLong と同じロジックだが Integer を返す。 */
    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** オブジェクトを JSONB 用 JSON 文字列に変換する。null・空リストは null を返す。 */
    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof List<?> list && list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON シリアライズ失敗: {}", e.getMessage());
            return null;
        }
    }

    private String keyField(HojinInfo.ChildMetadata metadata) {
        if (metadata == null) return null;
        String kf = metadata.getKeyField();
        return (kf == null || kf.isBlank()) ? null : kf;
    }
}
