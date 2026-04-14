package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.CompanyFullUpsertService;
import com.companylib.batch.infrastructure.gbizinfo.download.DownloadFileType;
import com.companylib.batch.infrastructure.gbizinfo.download.GBizInfoDownloadService;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBatchTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.batch.jdbc.initialize-schema=always",
    "gbizinfo.api.base-url=http://localhost",
    "gbizinfo.api.token=test-token",
    "gbizinfo.download.api-url=http://localhost/download",
    "gbizinfo.download.dir=/tmp/test-gbizinfo"
})
class GBizInfoInitialLoadJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier(GBizInfoInitialLoadJobConfig.JOB_NAME)
    private Job initialLoadJob;

    @MockBean
    private GBizInfoDownloadService downloadService;

    @MockBean
    private CompanyFullUpsertService companyFullUpsertService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(initialLoadJob);
    }

    @Test
    @DisplayName("有効な Hojinjoho JSON を処理してジョブが COMPLETED になる")
    void initialLoadJob_completesSuccessfully() throws Exception {
        setupAllMocks(createHojinjohoTestDir(), createEmptyKihonjohoDir(), createEmptyKessanjohoDir());

        JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("有効な 2 件に対して upsert が 2 回呼ばれる（無効 2 件はフィルタされる）")
    void initialLoadJob_callsUpsertForValidRecordsOnly() throws Exception {
        setupAllMocks(createHojinjohoTestDir(), createEmptyKihonjohoDir(), createEmptyKessanjohoDir());

        JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // corporate_number=null と name=null の 2 件はプロセッサがスキップ → 残り 2 件のみ
        verify(companyFullUpsertService, times(2)).upsert(any(HojinInfo.class));
    }

    @Test
    @DisplayName("ダウンロードサービスが例外をスローした場合、ジョブが FAILED になる")
    void initialLoadJob_downloadFailure_failsJob() throws Exception {
        when(downloadService.downloadAndExtract(DownloadFileType.HOJINJOHO))
            .thenThrow(new IOException("ダウンロード失敗"));

        JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    @Test
    @DisplayName("Hojinjoho JSON が存在しないディレクトリを指定した場合、ジョブが FAILED になる")
    void initialLoadJob_emptyHojinjohoDir_failsJob() throws Exception {
        Path emptyDir = tempDir.resolve("empty");
        java.nio.file.Files.createDirectories(emptyDir);

        setupAllMocks(emptyDir, createEmptyKihonjohoDir(), createEmptyKessanjohoDir());

        JobParameters params = new JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    // ── ヘルパー ──────────────────────────────────────────────────────────────

    private void setupAllMocks(Path hojinjohoDir, Path kihonjohoDir, Path kessanjohoDir)
        throws IOException {
        when(downloadService.downloadAndExtract(DownloadFileType.HOJINJOHO))
            .thenReturn(hojinjohoDir);
        when(downloadService.downloadAndExtract(DownloadFileType.KIHONJOHO))
            .thenReturn(kihonjohoDir);
        when(downloadService.downloadAndExtract(DownloadFileType.KESSANJOHO))
            .thenReturn(kessanjohoDir);
    }

    /** テスト用 Hojinjoho JSON ファイルを含むディレクトリを作成 */
    private Path createHojinjohoTestDir() throws IOException {
        Path dir = tempDir.resolve("Hojinjoho_extracted");
        java.nio.file.Files.createDirectories(dir);

        URL resource = getClass().getClassLoader().getResource("test-hojin.json");
        assertThat(resource).as("テストリソース test-hojin.json が見つかりません").isNotNull();
        try {
            java.nio.file.Files.copy(
                Path.of(resource.toURI()),
                dir.resolve("Hojinjoho_01.json"),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        } catch (java.net.URISyntaxException e) {
            throw new IOException("テストリソースのパス解決に失敗: " + resource, e);
        }
        return dir;
    }

    /** テスト用 Kihonjoho ディレクトリを作成（ヘッダーのみの空 CSV = 0 件処理） */
    private Path createEmptyKihonjohoDir() throws IOException {
        Path dir = tempDir.resolve("Kihonjoho_extracted");
        java.nio.file.Files.createDirectories(dir);
        // BOM 付き UTF-8 ヘッダー行のみ（データ行なし → 0 件処理）
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] header = ("\ufeff\"法人番号\",商号または名称,商号または名称（カナ）,商号または名称（英字）,"
            + "登記記録の閉鎖等年月日,登記記録の閉鎖等の事由,登記住所,郵便番号,"
            + "都道府県,都道府県コード,市区町村（郡）,市区町村コード,番地以下,"
            + "組織種別,処理区分,訂正区分,状態,代表者名称,"
            + "資本金,従業員数,企業規模詳細(男性),企業規模詳細(女性),"
            + "事業概要,WebサイトURL,創業年,事業種目,設立年月日,"
            + "全省庁統一資格-資格等級,全省庁統一資格-営業品目,更新年月日,キー情報\n")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.nio.file.Files.write(dir.resolve("Kihonjoho_UTF-8.csv"), header);
        return dir;
    }

    /** テスト用空 Kessanjoho ディレクトリを作成 */
    private Path createEmptyKessanjohoDir() throws IOException {
        Path dir = tempDir.resolve("Kessanjoho_extracted");
        java.nio.file.Files.createDirectories(dir);
        return dir;
    }
}
