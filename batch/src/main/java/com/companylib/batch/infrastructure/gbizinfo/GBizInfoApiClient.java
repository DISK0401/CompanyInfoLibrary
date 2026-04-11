package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfoResponse;
import com.companylib.batch.infrastructure.gbizinfo.dto.UpdateInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
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

    /** 法人基本情報を1件取得 */
    public HojinInfoResponse getHojinInfo(String corporateNumber) {
        String url = baseUrl + "/hojin/" + corporateNumber;
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<HojinInfoResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, HojinInfoResponse.class
        );
        return response.getBody();
    }

    /**
     * 更新法人番号リストを取得（日次差分）
     * @param from 取得開始日 yyyy-MM-dd
     * @param to   取得終了日 yyyy-MM-dd
     */
    public UpdateInfoResponse getUpdateInfo(String from, String to) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/hojin/updateInfo")
            .queryParam("from", from)
            .queryParam("to", to)
            .toUriString();

        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("gBizINFO updateInfo 取得: from={}, to={}", from, to);
        ResponseEntity<UpdateInfoResponse> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, UpdateInfoResponse.class
        );
        return response.getBody();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-hojinInfo-api-token", apiToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
