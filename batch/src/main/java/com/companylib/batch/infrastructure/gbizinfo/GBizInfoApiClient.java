package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfoResponse;
import com.companylib.batch.infrastructure.gbizinfo.dto.UpdateInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GBizInfoApiClient {

    private final RestTemplate restTemplate;

    private static final long RATE_LIMIT_WAIT_MS = 10_000L;
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Value("${gbizinfo.api.base-url}")
    private String baseUrl;

    @Value("${gbizinfo.api.token}")
    private String apiToken;

    @Value("${gbizinfo.api.request-interval-ms:500}")
    private long requestIntervalMs;

    /**
     * 法人基本情報を1件取得。
     * @return 取得結果。404 の場合は null を返す。
     * @throws RuntimeException 429（レート制限）または 5xx の場合
     */
    public HojinInfoResponse getHojinInfo(String corporateNumber) {
        String url = baseUrl + "/hojin/" + corporateNumber;
        try {
            ResponseEntity<HojinInfoResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), HojinInfoResponse.class
            );
            HojinInfoResponse body = response.getBody();
            if (body == null) {
                log.warn("レスポンスボディが空: corporateNumber={}", corporateNumber);
                return null;
            }
            sleepInterval();
            return body;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("法人情報なし (404): corporateNumber={}", corporateNumber);
                return null;
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("レート制限 (429): corporateNumber={}", corporateNumber);
                sleepForRateLimit();
                throw new RuntimeException("gBizINFO API レート制限: " + corporateNumber, e);
            }
            log.error("クライアントエラー ({}): corporateNumber={}", e.getStatusCode(), corporateNumber);
            throw new RuntimeException("gBizINFO API クライアントエラー: " + corporateNumber, e);
        } catch (HttpServerErrorException e) {
            log.error("サーバーエラー ({}): corporateNumber={}", e.getStatusCode(), corporateNumber);
            sleepForRateLimit();
            throw new RuntimeException("gBizINFO API サーバーエラー: " + corporateNumber, e);
        }
    }

    /**
     * 更新法人番号リストを取得（日次差分）。
     * @param from 取得開始日 yyyy-MM-dd
     * @param to   取得終了日 yyyy-MM-dd
     * @param page ページ番号（1始まり）
     * @throws RuntimeException API エラーの場合
     */
    public UpdateInfoResponse getUpdateInfo(String from, String to, int page) {
        // API は yyyyMMdd 形式を要求するため、yyyy-MM-dd 形式で渡された場合は変換する
        String apiFrom = toApiDateFormat(from);
        String apiTo = toApiDateFormat(to);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/hojin/updateInfo")
            .queryParam("from", apiFrom)
            .queryParam("to", apiTo)
            .queryParam("page", page)
            .toUriString();

        log.debug("gBizINFO updateInfo 取得: from={}, to={}, page={}", apiFrom, apiTo, page);
        try {
            ResponseEntity<UpdateInfoResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), UpdateInfoResponse.class
            );
            UpdateInfoResponse body = response.getBody();
            if (body == null) {
                log.warn("updateInfo レスポンスボディが空: from={}, to={}, page={}", from, to, page);
                return new UpdateInfoResponse();
            }
            sleepInterval();
            return body;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("updateInfo 対象データなし (404): from={}, to={}, page={}", apiFrom, apiTo, page);
                return new UpdateInfoResponse();
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("レート制限 (429): updateInfo from={}, to={}, page={}", apiFrom, apiTo, page);
                sleepForRateLimit();
                throw new RuntimeException("gBizINFO API レート制限 (updateInfo)", e);
            }
            log.error("updateInfo クライアントエラー ({}): from={}, to={}, page={}", e.getStatusCode(), apiFrom, apiTo, page);
            throw new RuntimeException("gBizINFO API クライアントエラー (updateInfo)", e);
        } catch (HttpServerErrorException e) {
            log.error("updateInfo サーバーエラー ({}): from={}, to={}, page={}", e.getStatusCode(), apiFrom, apiTo, page);
            sleepForRateLimit();
            throw new RuntimeException("gBizINFO API サーバーエラー (updateInfo)", e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-hojinInfo-api-token", apiToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    /** yyyy-MM-dd 形式の日付文字列を API 要求の yyyyMMdd 形式に変換する。 */
    private String toApiDateFormat(String date) {
        return LocalDate.parse(date, ISO_DATE_FORMAT).format(API_DATE_FORMAT);
    }

    /** 通常リクエスト後のインターバル待機。 */
    private void sleepInterval() {
        try {
            Thread.sleep(requestIntervalMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * レート制限・サーバーエラー時のウェイト。
     * Spring Batch のリトライ機構にバックオフ設定がないため、
     * 例外をスローする前に待機してリトライ間隔を確保する。
     */
    private void sleepForRateLimit() {
        try {
            log.info("API 待機中（{}ms）...", RATE_LIMIT_WAIT_MS);
            Thread.sleep(RATE_LIMIT_WAIT_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
