package com.companylib.batch.infrastructure.gbizinfo;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
public class GBizInfoConfig {

    @Value("${gbizinfo.proxy.host:}")
    private String proxyHost;

    @Value("${gbizinfo.proxy.port:0}")
    private int proxyPort;

    @Value("${gbizinfo.proxy.user:}")
    private String proxyUser;

    @Value("${gbizinfo.proxy.password:}")
    private String proxyPassword;

    @Value("${gbizinfo.ssl.verify:true}")
    private boolean sslVerify;

    @PostConstruct
    public void logConfig() {
        log.info("=== gBizINFO 接続設定 ===");
        log.info("  SSL 証明書検証: {}", sslVerify ? "有効 (GBIZINFO_SSL_VERIFY=true)" : "無効 (GBIZINFO_SSL_VERIFY=false)");
        log.info("  プロキシ: {}", hasProxy() ? proxyHost + ":" + proxyPort : "なし (GBIZINFO_PROXY_HOST 未設定)");
        log.info("========================");
        if (sslVerify) {
            log.warn("SSL 証明書検証が有効です。社内プロキシで証明書エラーが発生する場合は "
                + "GBIZINFO_SSL_VERIFY=false を設定してください。");
        }
    }

    /** API 呼び出し用 RestTemplate（短いタイムアウト） */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return buildRestTemplate(10_000, 30_000);
    }

    /** ダウンロード用 RestTemplate（長いタイムアウト・大容量ファイル対応） */
    @Bean("downloadRestTemplate")
    public RestTemplate downloadRestTemplate() {
        return buildRestTemplate(30_000, 30 * 60_000);
    }

    /**
     * @param connectTimeoutMs 接続タイムアウト（ミリ秒）
     * @param responseTimeoutMs レスポンスタイムアウト（ミリ秒）
     */
    private RestTemplate buildRestTemplate(int connectTimeoutMs, int responseTimeoutMs) {
        try {
            HttpClientBuilder builder = HttpClients.custom();

            // ── SSL 設定 ──────────────────────────────────────────────────────
            if (!sslVerify) {
                SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

                SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

                builder.setConnectionManager(
                    PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build()
                );
                log.warn("SSL 証明書検証が無効化されています (GBIZINFO_SSL_VERIFY=false)。"
                    + "本番環境では使用しないでください。");
            }

            // ── プロキシ設定 ──────────────────────────────────────────────────
            if (hasProxy()) {
                builder.setProxy(new HttpHost(proxyHost, proxyPort));
                log.info("プロキシ設定: {}:{}", proxyHost, proxyPort);

                if (hasProxyAuth()) {
                    BasicCredentialsProvider cp = new BasicCredentialsProvider();
                    cp.setCredentials(
                        new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword.toCharArray())
                    );
                    builder.setDefaultCredentialsProvider(cp);
                    log.info("プロキシ認証設定: user={}", proxyUser);
                }
            }

            // ── タイムアウト設定 ───────────────────────────────────────────────
            builder.setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                    .setResponseTimeout(Timeout.ofMilliseconds(responseTimeoutMs))
                    .build()
            );

            HttpClient httpClient = builder.build();

            HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(connectTimeoutMs);

            return new RestTemplate(factory);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("RestTemplate の初期化に失敗しました", e);
        }
    }

    private boolean hasProxy() {
        return proxyHost != null && !proxyHost.isBlank() && proxyPort > 0;
    }

    private boolean hasProxyAuth() {
        return proxyUser != null && !proxyUser.isBlank();
    }
}
