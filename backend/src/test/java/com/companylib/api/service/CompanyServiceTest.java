package com.companylib.api.service;

import com.companylib.api.domain.dto.CompanyDetailDto;
import com.companylib.api.domain.dto.CompanySearchRequest;
import com.companylib.api.domain.dto.CompanySummaryDto;
import com.companylib.api.domain.dto.PageResponse;
import com.companylib.api.domain.entity.Company;
import com.companylib.api.repository.CompanyRepository;
import com.companylib.api.repository.CompanyWorkplaceRepository;
import com.companylib.api.repository.spec.FuzzyQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyWorkplaceRepository companyWorkplaceRepository;

    @Mock
    private FuzzyQueryBuilder fuzzyQueryBuilder;

    @InjectMocks
    private CompanyService companyService;

    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        sampleCompany = new Company();
        sampleCompany.setCorporateNumber("1234567890123");
        sampleCompany.setName("テスト株式会社");
        sampleCompany.setLocation("東京都千代田区");
        sampleCompany.setCapitalStock(10_000_000L);
        sampleCompany.setEmployeeNumber(100);
    }

    @Nested
    @DisplayName("search()")
    class SearchTest {

        @Test
        @DisplayName("name 条件を渡すと Repository を呼び出し結果を返す")
        void search_returnsResults() {
            CompanySearchRequest req = new CompanySearchRequest();
            req.setName("テスト");
            req.setPage(0);
            req.setSize(20);

            Page<Company> page = new PageImpl<>(List.of(sampleCompany), PageRequest.of(0, 20), 1);
            when(companyRepository.searchCompanies(eq("テスト"), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

            PageResponse<CompanySummaryDto> result = companyService.search(req);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCorporateNumber()).isEqualTo("1234567890123");
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("検索結果が 0 件のとき空のページを返す")
        void search_returnsEmptyPage() {
            CompanySearchRequest req = new CompanySearchRequest();
            req.setPage(0);
            req.setSize(20);

            Page<Company> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(companyRepository.searchCompanies(any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

            PageResponse<CompanySummaryDto> result = companyService.search(req);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("size を指定するとそのまま Repository に渡す")
        void search_usesRequestedSize() {
            CompanySearchRequest req = new CompanySearchRequest();
            req.setSize(50);

            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 50), 0);
            when(companyRepository.searchCompanies(any(), any(), any(), any(),
                argThat(p -> p.getPageSize() == 50)))
                .thenReturn(page);

            companyService.search(req);

            verify(companyRepository).searchCompanies(
                any(), any(), any(), any(),
                argThat(pageable -> pageable.getPageSize() == 50)
            );
        }

        @Test
        @DisplayName("location・minCapital・minEmployees パラメータが Repository に正しく渡される")
        void search_passesAllParamsToRepository() {
            CompanySearchRequest req = new CompanySearchRequest();
            req.setName("テスト");
            req.setLocation("東京都");
            req.setMinCapital(1_000_000L);
            req.setMinEmployees(10);
            req.setPage(0);
            req.setSize(20);

            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(companyRepository.searchCompanies(any(), any(), any(), any(), any()))
                .thenReturn(page);

            companyService.search(req);

            verify(companyRepository).searchCompanies(
                eq("テスト"),
                eq("東京都"),
                eq(1_000_000L),
                eq(10),
                any(Pageable.class)
            );
        }

        @Test
        @DisplayName("検索結果は capitalStock の降順でソートされる")
        void search_sortsBy_capitalStockDesc() {
            CompanySearchRequest req = new CompanySearchRequest();
            req.setPage(0);
            req.setSize(20);

            Page<Company> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(companyRepository.searchCompanies(any(), any(), any(), any(), any()))
                .thenReturn(page);

            companyService.search(req);

            verify(companyRepository).searchCompanies(
                any(), any(), any(), any(),
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("capitalStock");
                    return order != null && order.isDescending();
                })
            );
        }
    }

    @Nested
    @DisplayName("getDetail()")
    class GetDetailTest {

        @Test
        @DisplayName("法人番号が存在する場合は CompanyDetailDto を返す")
        void getDetail_returnsDto() {
            when(companyRepository.findByIdWithDetails("1234567890123"))
                .thenReturn(Optional.of(sampleCompany));
            when(companyWorkplaceRepository.findByCompanyCorporateNumber("1234567890123"))
                .thenReturn(Optional.empty());

            CompanyDetailDto dto = companyService.getDetail("1234567890123");

            assertThat(dto.getCorporateNumber()).isEqualTo("1234567890123");
            assertThat(dto.getName()).isEqualTo("テスト株式会社");
        }

        @Test
        @DisplayName("法人番号が存在しない場合は CompanyNotFoundException をスロー")
        void getDetail_throwsNotFoundException() {
            when(companyRepository.findByIdWithDetails("9999999999999"))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.getDetail("9999999999999"))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessageContaining("9999999999999");
        }
    }
}
