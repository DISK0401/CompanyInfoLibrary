package com.companylib.batch.infrastructure.gbizinfo.download;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * gBizINFO ダウンロードサイトから ZIP ファイルを取得し、解凍するサービス。
 *
 * <p>スキップロジック:
 * <ol>
 *   <li>ダウンロードディレクトリ内に同一種別の ZIP が存在する場合、
 *       まず新しいファイルをダウンロードしてファイル名（日付）を比較する。</li>
 *   <li>既存 ZIP と日付が同じ場合はスキップして既存ファイルを使用する。</li>
 *   <li>新しい日付の ZIP がダウンロードされた場合は既存を置き換える。</li>
 * </ol>
 */
@Slf4j
@Service
public class GBizInfoDownloadService {

    private final RestTemplate restTemplate;

    public GBizInfoDownloadService(@Qualifier("downloadRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${gbizinfo.download.api-url}")
    private String downloadApiUrl;

    @Value("${gbizinfo.download.dir}")
    private String downloadDir;

    @Value("${gbizinfo.api.token}")
    private String apiToken;

    @PostConstruct
    public void validateConfig() {
        if (apiToken == null || apiToken.isBlank()) {
            throw new IllegalStateException(
                "gBizINFO API トークンが設定されていません。"
                + "環境変数 GBIZINFO_API_TOKEN を設定してください。");
        }
    }

    /** ファイル名から日付部分（yyyyMMdd）を抽出するパターン */
    private static final Pattern DATE_PATTERN = Pattern.compile("_(\\d{8})(?:\\.zip)?$");

    /**
     * 指定ファイル種別の ZIP をダウンロードして解凍する。
     *
     * @param fileType ダウンロード対象のファイル種別
     * @return 解凍したディレクトリの Path
     * @throws IOException ダウンロードまたは解凍失敗時
     */
    public Path downloadAndExtract(DownloadFileType fileType) throws IOException {
        Path dir = Path.of(downloadDir);
        Files.createDirectories(dir);

        log.info("[{}] ダウンロード処理開始", fileType.getDownFileName());

        Path zipPath = resolveZipPath(fileType, dir);
        Path extractDir = dir.resolve(fileType.getDownFileName() + "_extracted");

        log.info("[{}] ZIP パス: {}", fileType.getDownFileName(), zipPath);

        extractZip(zipPath, extractDir);
        log.info("[{}] 解凍完了: {}", fileType.getDownFileName(), extractDir);

        return extractDir;
    }

    /**
     * ZIP ファイルパスを解決する。既存ファイルとの比較でスキップ判定も行う。
     */
    private Path resolveZipPath(DownloadFileType fileType, Path dir) throws IOException {
        Optional<Path> existingZip = findExistingZip(dir, fileType);

        // 一時ファイルにダウンロード（未検査例外も含め確実に削除するために finally を使用）
        Path tempFile = Files.createTempFile(dir, fileType.getDownFileName() + "_", ".zip.tmp");
        String downloadedFileName;
        try {
            log.info("[{}] ダウンロード中...", fileType.getDownFileName());
            downloadedFileName = downloadZipToFile(fileType, tempFile);
            log.info("[{}] ダウンロード完了: {}", fileType.getDownFileName(), downloadedFileName);
        } catch (Exception e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("ZIP ダウンロード失敗: fileType=" + fileType.getDownFileName(), e);
        }

        Path targetPath = dir.resolve(downloadedFileName);

        if (existingZip.isPresent()) {
            String existingName = existingZip.get().getFileName().toString();
            String newName = downloadedFileName;

            if (existingName.equals(newName)) {
                // 同じファイル名 → スキップ
                Files.deleteIfExists(tempFile);
                log.info("[{}] 既存 ZIP を使用（スキップ）: {}", fileType.getDownFileName(), existingName);
                return existingZip.get();
            } else if (isNewer(newName, existingName)) {
                // 新しいファイル → 既存を削除して置換
                Files.deleteIfExists(existingZip.get());
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("[{}] 新しい ZIP に更新: {} → {}", fileType.getDownFileName(), existingName, newName);
                return targetPath;
            } else {
                // 古いファイル → スキップ
                Files.deleteIfExists(tempFile);
                log.info("[{}] 既存 ZIP の方が新しいため使用: {}", fileType.getDownFileName(), existingName);
                return existingZip.get();
            }
        } else {
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath;
        }
    }

    /**
     * ダウンロード API に POST し、レスポンスをストリーミングでファイルに書き込む。
     * Content-Disposition ヘッダーからファイル名を返す。
     * byte[] に全量ロードしないことで大容量ファイルの OOM を防止する。
     */
    private String downloadZipToFile(DownloadFileType fileType, Path saveTo) throws IOException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("downfile", fileType.getDownFileName());
        body.add("meta", "META");
        body.add("downenc", "UTF-8");
        body.add("apiToken", apiToken);
        body.add("isZip", "on");
        body.add("downtype", "zip");

        AtomicReference<String> fileNameRef = new AtomicReference<>();

        restTemplate.execute(
            downloadApiUrl,
            HttpMethod.POST,
            clientRequest -> {
                clientRequest.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                // フォームボディを手動エンコード（特殊文字を含むトークン等を正しく送信するためURLエンコード必須）
                StringBuilder sb = new StringBuilder();
                body.forEach((key, values) -> values.forEach(val -> {
                    if (!sb.isEmpty()) sb.append('&');
                    sb.append(URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
                      .append('=')
                      .append(URLEncoder.encode(val, java.nio.charset.StandardCharsets.UTF_8));
                }));
                clientRequest.getBody().write(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            },
            response -> {
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new IOException("ダウンロード失敗: status=" + response.getStatusCode()
                        + ", fileType=" + fileType.getDownFileName());
                }
                // ファイル名取得
                fileNameRef.set(extractFileName(response.getHeaders(), fileType));
                // ストリーミングで書き込み
                try (OutputStream os = Files.newOutputStream(saveTo,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    response.getBody().transferTo(os);
                }
                return null;
            }
        );

        return fileNameRef.get();
    }

    /**
     * Content-Disposition ヘッダーからファイル名を取得。取得できない場合はフォールバック名を返す。
     */
    private String extractFileName(HttpHeaders headers, DownloadFileType fileType) {
        ContentDisposition cd = headers.getContentDisposition();
        if (cd.getFilename() != null && !cd.getFilename().isBlank()) {
            return cd.getFilename();
        }
        // フォールバック: fileType名 + ".zip"
        log.warn("[{}] Content-Disposition からファイル名を取得できませんでした。フォールバック名を使用します。",
            fileType.getDownFileName());
        return fileType.getDownFileName() + ".zip";
    }

    /**
     * ダウンロードディレクトリ内から指定種別の ZIP を検索する。
     */
    private Optional<Path> findExistingZip(Path dir, DownloadFileType fileType) throws IOException {
        String prefix = fileType.getDownFileName();
        try (Stream<Path> files = Files.list(dir)) {
            return files
                .filter(p -> p.getFileName().toString().startsWith(prefix)
                    && p.getFileName().toString().endsWith(".zip"))
                .max(Comparator.comparing(p -> extractDate(p.getFileName().toString())));
        }
    }

    /**
     * ファイル名が新しいかどうかを日付文字列で比較する。
     */
    private boolean isNewer(String newName, String existingName) {
        String newDate = extractDate(newName);
        String existingDate = extractDate(existingName);
        return newDate.compareTo(existingDate) > 0;
    }

    /** ファイル名から日付（yyyyMMdd）を抽出。見つからない場合は空文字を返す。 */
    private String extractDate(String fileName) {
        Matcher m = DATE_PATTERN.matcher(fileName);
        return m.find() ? m.group(1) : "";
    }

    /**
     * ZIP ファイルを指定ディレクトリに解凍する（既存内容は上書き）。
     */
    private void extractZip(Path zipFile, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        log.info("ZIP 解凍中: {} → {}", zipFile, targetDir);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            int count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();

                // Zip-slip 対策
                if (!entryPath.startsWith(targetDir)) {
                    throw new IOException("不正なZIPエントリ（パストラバーサル）: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (OutputStream os = Files.newOutputStream(entryPath,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        zis.transferTo(os);
                    }
                    count++;
                }
                zis.closeEntry();
            }
            log.info("ZIP 解凍完了: {}件のファイルを展開", count);
        }
    }
}
