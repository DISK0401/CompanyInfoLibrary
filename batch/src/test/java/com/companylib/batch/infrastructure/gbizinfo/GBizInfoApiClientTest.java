package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfoResponse;
import com.companylib.batch.infrastructure.gbizinfo.dto.UpdateInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GBizInfoApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GBizInfoApiClient apiClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apiClient, "baseUrl", "https://api.test.example.com/hojin/v2");
        ReflectionTestUtils.setField(apiClient, "apiToken", "test-token");
        ReflectionTestUtils.setField(apiClient, "requestIntervalMs", 0L);
    }

    @Nested
    @DisplayName("getHojinInfo()")
    class GetHojinInfoTest {

        @Test
        @DisplayName("正常レスポンスで HojinInfoResponse を返す")
        void success_returnsResponse() {
            HojinInfo info = new HojinInfo();
            info.setCorporateNumber("1234567890123");
            info.setName("テスト株式会社");

            HojinInfoResponse body = new HojinInfoResponse();
            body.setHojinInfos(List.of(info));

            when(restTemplate.exchange(
                contains("/hojin/1234567890123"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(HojinInfoResponse.class)
            )).thenReturn(ResponseEntity.ok(body));

            HojinInfoResponse result = apiClient.getHojinInfo("1234567890123");

            assertThat(result).isNotNull();
            assertThat(result.getHojinInfos()).hasSize(1);
            assertThat(result.getHojinInfos().get(0).getCorporateNumber()).isEqualTo("1234567890123");
        }

        @Test
        @DisplayName("404 レスポンスで null を返す")
        void notFound_returnsNull() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(HojinInfoResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

            HojinInfoResponse result = apiClient.getHojinInfo("9999999999999");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("429 レスポンスで RuntimeException をスロー")
        void rateLimited_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(HojinInfoResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getHojinInfo("1234567890123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("レート制限");
        }

        @Test
        @DisplayName("5xx レスポンスで RuntimeException をスロー")
        void serverError_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(HojinInfoResponse.class)))
                .thenThrow(HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getHojinInfo("1234567890123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("サーバーエラー");
        }

        @Test
        @DisplayName("レスポンスボディが null のとき null を返す")
        void nullBody_returnsNull() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(HojinInfoResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

            HojinInfoResponse result = apiClient.getHojinInfo("1234567890123");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("404・429 以外の 4xx（例: 401 Unauthorized）で RuntimeException をスロー")
        void otherClientError_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(HojinInfoResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getHojinInfo("1234567890123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("クライアントエラー");
        }

        @Test
        @DisplayName("API トークンが X-hojinInfo-api-token ヘッダーに設定される")
        void sendsApiTokenHeader() {
            HojinInfoResponse body = new HojinInfoResponse();
            body.setHojinInfos(List.of(new HojinInfo()));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<HttpEntity<Void>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            when(restTemplate.exchange(
                any(String.class), eq(HttpMethod.GET), entityCaptor.capture(), eq(HojinInfoResponse.class)
            )).thenReturn(ResponseEntity.ok(body));

            apiClient.getHojinInfo("1234567890123");

            assertThat(entityCaptor.getValue().getHeaders().getFirst("X-hojinInfo-api-token"))
                .isEqualTo("test-token");
        }
    }

    @Nested
    @DisplayName("getUpdateInfo()")
    class GetUpdateInfoTest {

        @Test
        @DisplayName("正常レスポンスで UpdateInfoResponse を返す")
        void success_returnsResponse() {
            HojinInfo info = new HojinInfo();
            info.setCorporateNumber("1234567890123");

            UpdateInfoResponse body = new UpdateInfoResponse();
            body.setHojinInfos(List.of(info));

            when(restTemplate.exchange(
                contains("updateInfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UpdateInfoResponse.class)
            )).thenReturn(ResponseEntity.ok(body));

            UpdateInfoResponse result = apiClient.getUpdateInfo("2024-01-01", "2024-01-02", 1);

            assertThat(result.getHojinInfos()).hasSize(1);
            assertThat(result.getHojinInfos().get(0).getCorporateNumber()).isEqualTo("1234567890123");
        }

        @Test
        @DisplayName("レスポンスボディが null のとき空の UpdateInfoResponse を返す")
        void nullBody_returnsEmptyResponse() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(UpdateInfoResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

            UpdateInfoResponse result = apiClient.getUpdateInfo("2024-01-01", "2024-01-02", 1);

            assertThat(result).isNotNull();
            assertThat(result.getHojinInfos()).isNull();
        }

        @Test
        @DisplayName("429 レスポンスで RuntimeException をスロー")
        void rateLimited_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(UpdateInfoResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getUpdateInfo("2024-01-01", "2024-01-02", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("レート制限");
        }

        @Test
        @DisplayName("5xx レスポンスで RuntimeException をスロー")
        void serverError_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(UpdateInfoResponse.class)))
                .thenThrow(HttpServerErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getUpdateInfo("2024-01-01", "2024-01-02", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("サーバーエラー");
        }

        @Test
        @DisplayName("404・429 以外の 4xx で RuntimeException をスロー")
        void otherClientError_throwsRuntimeException() {
            when(restTemplate.exchange(any(String.class), any(), any(HttpEntity.class), eq(UpdateInfoResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN, "Forbidden", HttpHeaders.EMPTY, null, null));

            assertThatThrownBy(() -> apiClient.getUpdateInfo("2024-01-01", "2024-01-02", 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("クライアントエラー");
        }
    }
}
