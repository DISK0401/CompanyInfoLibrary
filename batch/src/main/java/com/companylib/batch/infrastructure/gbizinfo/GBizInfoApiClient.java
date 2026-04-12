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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GBizInfoApiClient {

    private final RestTemplate restTemplate;

    @Value("${gbizinfo.api.base-url}")
    private String baseUrl;

    @Value("${gbizinfo.api.token}")
    private String apiToken;

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
            return body;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.debug("法人情報なし (404): corporateNumber={}", corporateNumber);
                return null;
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("レート制限 (429): corporateNumber={}", corporateNumber);
                throw new RuntimeException("gBizINFO API レート制限: " + corporateNumber, e);
            }
            log.error("クライアントエラー ({}): corporateNumber={}", e.getStatusCode(), corporateNumber);
            throw new RuntimeException("gBizINFO API クライアントエラー: " + corporateNumber, e);
        } catch (HttpServerErrorException e) {
            log.error("サーバーエラー ({}): corporateNumber={}", e.getStatusCode(), corporateNumber);
            throw new RuntimeException("gBizINFO API サーバーエラー: " + corporateNumber, e);
        }
    }

    /**
     * 更新法人番号リストを取得（日次差分）。
     * @param from 取得開始日 yyyy-MM-dd
     * @param to   取得終了日 yyyy-MM-dd
     * @throws RuntimeException API エラーの場合
     */
    public UpdateInfoResponse getUpdateInfo(String from, String to) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/hojin/updateInfo")
            .queryParam("from", from)
            .queryParam("to", to)
            .toUriString();

        log.info("gBizINFO updateInfo 取得: from={}, to={}", from, to);
        try {
            ResponseEntity<UpdateInfoResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), UpdateInfoResponse.class
            );
            UpdateInfoResponse body = response.getBody();
            if (body == null) {
                log.warn("updateInfo レスポンスボディが空: from={}, to={}", from, to);
                return new UpdateInfoResponse();
            }
            return body;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("レート制限 (429): updateInfo from={}, to={}", from, to);
                throw new RuntimeException("gBizINFO API レート制限 (updateInfo)", e);
            }
            log.error("updateInfo クライアントエラー ({}): from={}, to={}", e.getStatusCode(), from, to);
            throw new RuntimeException("gBizINFO API クライアントエラー (updateInfo)", e);
        } catch (HttpServerErrorException e) {
            log.error("updateInfo サーバーエラー ({}): from={}, to={}", e.getStatusCode(), from, to);
            throw new RuntimeException("gBizINFO API サーバーエラー (updateInfo)", e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-hojinInfo-api-token", apiToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
