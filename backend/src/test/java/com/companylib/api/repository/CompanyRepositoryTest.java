package com.companylib.api.repository;

import com.companylib.api.domain.entity.Company;
import com.companylib.api.domain.entity.CompanyBusinessItem;
import com.companylib.api.domain.entity.CompanyFinance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CompanyRepository の JPQL クエリを H2 インメモリ DB で検証する。
 * @DataJpaTest は Flyway を無効化し、エンティティ定義から DDL を自動生成する。
 */
@DataJpaTest
class CompanyRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CompanyRepository companyRepository;

    // 各テストで共有するテストデータ（@BeforeEach でセットアップ）
    private Company companyA; // テスト株式会社   / 東京都千代田区 / 資本金 1000万 / 200名
    private Company companyB; // サンプル有限会社  / 大阪府大阪市   / 資本金  300万 / 30名
    private Company companyC; // サンプルテスト合同会社 / 東京都新宿区 / 資本金 null / 10名

    @BeforeEach
    void setUp() {
        companyA = createCompany("1111111111111", "テスト株式会社",       "東京都千代田区",   10_000_000L, 200);
        companyB = createCompany("2222222222222", "サンプル有限会社",      "大阪府大阪市北区",  3_000_000L,  30);
        companyC = createCompany("3333333333333", "サンプルテスト合同会社", "東京都新宿区",      null,        10);
        em.flush();
    }

    // -------------------------------------------------------
    // searchCompanies()
    // -------------------------------------------------------

    @Nested
    @DisplayName("searchCompanies()")
    class SearchCompaniesTest {

        private final Sort SORT_CAPITAL_DESC = Sort.by(Sort.Direction.DESC, "capitalStock");

        @Test
        @DisplayName("name 部分一致で該当企業のみ返す")
        void searchByName_returnsMatchingCompanies() {
            Page<Company> result = search("テスト", null, null, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                .extracting(Company::getCorporateNumber)
                .containsExactlyInAnyOrder("1111111111111", "3333333333333");
        }

        @Test
        @DisplayName("name 検索は大文字小文字を区別しない（LOWER 比較）")
        void searchByName_caseInsensitive() {
            createCompany("4444444444444", "ABC Corporation", "東京都港区", 1_000_000L, 50);
            em.flush();

            Page<Company> result = search("abc", null, null, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("ABC Corporation");
        }

        @Test
        @DisplayName("name に一致なしのとき空ページを返す")
        void searchByName_noMatch_returnsEmpty() {
            Page<Company> result = search("存在しない名称", null, null, null, 0, 20);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("location 部分一致で絞り込む")
        void searchByLocation_returnsMatchingCompanies() {
            Page<Company> result = search(null, "東京都", null, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                .extracting(Company::getCorporateNumber)
                .containsExactlyInAnyOrder("1111111111111", "3333333333333");
        }

        @Test
        @DisplayName("minCapital 以上の企業のみ返す")
        void searchByMinCapital_filtersBelow() {
            Page<Company> result = search(null, null, 5_000_000L, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCorporateNumber()).isEqualTo("1111111111111");
        }

        @Test
        @DisplayName("minEmployees 以上の企業のみ返す")
        void searchByMinEmployees_filtersBelow() {
            Page<Company> result = search(null, null, null, 100, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCorporateNumber()).isEqualTo("1111111111111");
        }

        @Test
        @DisplayName("複数条件は AND で組み合わせる")
        void searchWithMultipleParams_appliesAndCondition() {
            // name="サンプル" AND location="大阪" → companyB のみ
            Page<Company> result = search("サンプル", "大阪", null, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCorporateNumber()).isEqualTo("2222222222222");
        }

        @Test
        @DisplayName("全パラメータ null のとき全件返す")
        void searchWithNoParams_returnsAll() {
            Page<Company> result = search(null, null, null, null, 0, 20);

            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("capitalStock 降順でソートされる（null は最後）")
        void search_sortedByCapitalStockDesc_nullLast() {
            Page<Company> result = search(null, null, null, null, 0, 20);

            var content = result.getContent();
            assertThat(content.get(0).getCapitalStock()).isEqualTo(10_000_000L); // A
            assertThat(content.get(1).getCapitalStock()).isEqualTo(3_000_000L);  // B
            assertThat(content.get(2).getCapitalStock()).isNull();               // C（null は末尾）
        }

        @Test
        @DisplayName("ページネーション: page/size が正しく機能する")
        void search_paginationWorks() {
            Page<Company> page0 = search(null, null, null, null, 0, 2);
            Page<Company> page1 = search(null, null, null, null, 1, 2);

            assertThat(page0.getTotalElements()).isEqualTo(3);
            assertThat(page0.getTotalPages()).isEqualTo(2);
            assertThat(page0.getContent()).hasSize(2);
            assertThat(page1.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("minCapital=0 は資本金 null 以外の全件を返す")
        void searchByMinCapital_zero_excludesNull() {
            Page<Company> result = search(null, null, 0L, null, 0, 20);

            // capitalStock=null の companyC は除外される
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                .extracting(Company::getCorporateNumber)
                .containsExactlyInAnyOrder("1111111111111", "2222222222222");
        }

        private Page<Company> search(String name, String location, Long minCapital,
                                     Integer minEmployees, int page, int size) {
            return companyRepository.searchCompanies(
                name, location, minCapital, minEmployees,
                PageRequest.of(page, size, SORT_CAPITAL_DESC));
        }
    }

    // -------------------------------------------------------
    // findByIdWithDetails()
    // -------------------------------------------------------

    @Nested
    @DisplayName("findByIdWithDetails()")
    class FindByIdWithDetailsTest {

        @Test
        @DisplayName("財務情報・営業品目を LEFT JOIN FETCH で一括取得する")
        void findById_returnsCompanyWithAllRelations() {
            createFinance(companyA, "2023", 100_000_000L);
            createBusinessItem(companyA, "情報処理サービス");
            em.flush();
            em.clear(); // 1st-level cache を破棄してDB読み直し

            Optional<Company> result = companyRepository.findByIdWithDetails("1111111111111");

            assertThat(result).isPresent();
            Company c = result.get();
            assertThat(c.getName()).isEqualTo("テスト株式会社");
            assertThat(c.getFinances()).hasSize(1);
            assertThat(c.getFinances().get(0).getPeriod()).isEqualTo("2023");
            assertThat(c.getBusinessItems()).hasSize(1);
            assertThat(c.getBusinessItems().get(0).getItemName()).isEqualTo("情報処理サービス");
        }

        @Test
        @DisplayName("財務情報・営業品目がない企業は空リストで返す")
        void findById_withNoRelations_returnsEmptyLists() {
            em.flush();
            em.clear();

            Optional<Company> result = companyRepository.findByIdWithDetails("2222222222222");

            assertThat(result).isPresent();
            assertThat(result.get().getFinances()).isEmpty();
            assertThat(result.get().getBusinessItems()).isEmpty();
        }

        @Test
        @DisplayName("存在しない法人番号では Optional.empty() を返す")
        void findById_unknownId_returnsEmpty() {
            Optional<Company> result = companyRepository.findByIdWithDetails("9999999999999");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("財務情報が複数期分あっても全件取得する")
        void findById_multipleFinances_returnsAll() {
            createFinance(companyA, "2023", 100_000_000L);
            createFinance(companyA, "2022",  90_000_000L);
            createFinance(companyA, "2021",  80_000_000L);
            em.flush();
            em.clear();

            Optional<Company> result = companyRepository.findByIdWithDetails("1111111111111");

            assertThat(result).isPresent();
            assertThat(result.get().getFinances()).hasSize(3)
                .extracting(CompanyFinance::getPeriod)
                .containsExactlyInAnyOrder("2021", "2022", "2023");
        }

        @Test
        @DisplayName("DISTINCT により財務情報が重複しない（複数リレーション JOIN 時）")
        void findById_withBothRelations_noDuplication() {
            // finance 2件 × businessItem 2件 = 結合後4行 → DISTINCT で Company は1件
            createFinance(companyA, "2023", 100_000_000L);
            createFinance(companyA, "2022",  90_000_000L);
            createBusinessItem(companyA, "情報処理サービス");
            createBusinessItem(companyA, "コンサルティング");
            em.flush();
            em.clear();

            Optional<Company> result = companyRepository.findByIdWithDetails("1111111111111");

            assertThat(result).isPresent();
            assertThat(result.get().getFinances()).hasSize(2);
            assertThat(result.get().getBusinessItems()).hasSize(2);
        }
    }

    // -------------------------------------------------------
    // ヘルパーメソッド
    // -------------------------------------------------------

    private Company createCompany(String corporateNumber, String name, String location,
                                  Long capitalStock, Integer employeeNumber) {
        Company c = new Company();
        c.setCorporateNumber(corporateNumber);
        c.setName(name);
        c.setLocation(location);
        c.setCapitalStock(capitalStock);
        c.setEmployeeNumber(employeeNumber);
        return em.persist(c);
    }

    private CompanyFinance createFinance(Company company, String period, Long netSales) {
        CompanyFinance f = new CompanyFinance();
        f.setCompany(company);
        f.setPeriod(period);
        f.setNetSales(netSales);
        return em.persist(f);
    }

    private CompanyBusinessItem createBusinessItem(Company company, String itemName) {
        CompanyBusinessItem item = new CompanyBusinessItem();
        item.setCompany(company);
        item.setItemName(itemName);
        return em.persist(item);
    }
}
