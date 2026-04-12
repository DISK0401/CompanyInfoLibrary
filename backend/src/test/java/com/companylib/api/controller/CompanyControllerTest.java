package com.companylib.api.controller;

import com.companylib.api.domain.dto.*;
import com.companylib.api.service.CompanyNotFoundException;
import com.companylib.api.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Nested
    @DisplayName("GET /api/companies")
    class SearchTest {

        @Test
        @DisplayName("検索リクエストで 200 OK と結果を返す")
        void search_returns200() throws Exception {
            CompanySummaryDto summary = buildSummaryDto("1234567890123", "テスト株式会社");
            PageResponse<CompanySummaryDto> pageResp = new PageResponse<>(
                new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1)
            );
            when(companyService.search(any())).thenReturn(pageResp);

            mockMvc.perform(get("/api/companies")
                    .param("name", "テスト")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].corporateNumber").value("1234567890123"))
                .andExpect(jsonPath("$.content[0].name").value("テスト株式会社"))
                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("パラメータなしでも 200 OK を返す（デフォルト値を使用）")
        void search_withNoParams_returns200() throws Exception {
            PageResponse<CompanySummaryDto> empty = new PageResponse<>(
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0)
            );
            when(companyService.search(any())).thenReturn(empty);

            mockMvc.perform(get("/api/companies")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("size=0 はバリデーションエラーで 400 を返す（@Min(1) 違反）")
        void search_invalidSize_returns400() throws Exception {
            mockMvc.perform(get("/api/companies")
                    .param("size", "0")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.size").exists());
        }

        @Test
        @DisplayName("size=101 はバリデーションエラーで 400 を返す（@Max(100) 違反）")
        void search_sizeExceeds100_returns400() throws Exception {
            mockMvc.perform(get("/api/companies")
                    .param("size", "101")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.size").exists());
        }

        @Test
        @DisplayName("name が 200 文字超はバリデーションエラーで 400 を返す")
        void search_nameTooLong_returns400() throws Exception {
            String tooLong = "あ".repeat(201);
            mockMvc.perform(get("/api/companies")
                    .param("name", tooLong)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.name").exists());
        }

        @Test
        @DisplayName("location が 200 文字超はバリデーションエラーで 400 を返す")
        void search_locationTooLong_returns400() throws Exception {
            String tooLong = "あ".repeat(201);
            mockMvc.perform(get("/api/companies")
                    .param("location", tooLong)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.location").exists());
        }

        @Test
        @DisplayName("minCapital が負数はバリデーションエラーで 400 を返す")
        void search_negativeMinCapital_returns400() throws Exception {
            mockMvc.perform(get("/api/companies")
                    .param("minCapital", "-1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.minCapital").exists());
        }

        @Test
        @DisplayName("minEmployees が負数はバリデーションエラーで 400 を返す")
        void search_negativeMinEmployees_returns400() throws Exception {
            mockMvc.perform(get("/api/companies")
                    .param("minEmployees", "-1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.minEmployees").exists());
        }

        @Test
        @DisplayName("page が負数はバリデーションエラーで 400 を返す")
        void search_negativePage_returns400() throws Exception {
            mockMvc.perform(get("/api/companies")
                    .param("page", "-1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"))
                .andExpect(jsonPath("$.details.page").exists());
        }

        @Test
        @DisplayName("予期しない例外は 500 を返す")
        void search_unexpectedError_returns500() throws Exception {
            when(companyService.search(any()))
                .thenThrow(new RuntimeException("予期しないエラー"));

            mockMvc.perform(get("/api/companies")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("予期しないエラーが発生しました。"));
        }
    }

    @Nested
    @DisplayName("GET /api/companies/{corporateNumber}")
    class GetDetailTest {

        @Test
        @DisplayName("存在する法人番号で 200 OK と詳細を返す")
        void getDetail_returns200() throws Exception {
            CompanyDetailDto detail = buildDetailDto("1234567890123", "テスト株式会社");
            when(companyService.getDetail("1234567890123")).thenReturn(detail);

            mockMvc.perform(get("/api/companies/1234567890123")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corporateNumber").value("1234567890123"))
                .andExpect(jsonPath("$.name").value("テスト株式会社"));
        }

        @Test
        @DisplayName("存在しない法人番号で 404 を返す")
        void getDetail_notFound_returns404() throws Exception {
            when(companyService.getDetail("9999999999999"))
                .thenThrow(new CompanyNotFoundException("9999999999999"));

            mockMvc.perform(get("/api/companies/9999999999999")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("DB アクセスエラー時は 503 を返す")
        void getDetail_dbError_returns503() throws Exception {
            when(companyService.getDetail(any()))
                .thenThrow(new DataAccessResourceFailureException("DB connection failed"));

            mockMvc.perform(get("/api/companies/1234567890123")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("13桁以外の法人番号はバリデーションエラーで 400 を返す")
        void getDetail_invalidCorporateNumberFormat_returns400() throws Exception {
            mockMvc.perform(get("/api/companies/invalid")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("バリデーションエラー"));
        }

        @Test
        @DisplayName("IllegalArgumentException は 400 を返す")
        void getDetail_illegalArgument_returns400() throws Exception {
            when(companyService.getDetail("1234567890123"))
                .thenThrow(new IllegalArgumentException("不正な法人番号形式"));

            mockMvc.perform(get("/api/companies/1234567890123")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("不正な法人番号形式"));
        }

        @Test
        @DisplayName("予期しない例外は 500 を返す")
        void getDetail_unexpectedError_returns500() throws Exception {
            when(companyService.getDetail(any()))
                .thenThrow(new RuntimeException("予期しないエラー"));

            mockMvc.perform(get("/api/companies/1234567890123")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("予期しないエラーが発生しました。"));
        }
    }

    // ---- ヘルパー ----

    private CompanySummaryDto buildSummaryDto(String corporateNumber, String name) {
        com.companylib.api.domain.entity.Company c = new com.companylib.api.domain.entity.Company();
        c.setCorporateNumber(corporateNumber);
        c.setName(name);
        return new CompanySummaryDto(c);
    }

    private CompanyDetailDto buildDetailDto(String corporateNumber, String name) {
        com.companylib.api.domain.entity.Company c = new com.companylib.api.domain.entity.Company();
        c.setCorporateNumber(corporateNumber);
        c.setName(name);
        return new CompanyDetailDto(c);
    }
}
