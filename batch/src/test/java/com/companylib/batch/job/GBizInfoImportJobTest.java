package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.CompanyUpsertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.TestPropertySource;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBatchTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    // テスト用インメモリ DB（H2 PostgreSQL 互換モード）
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    // JPA が H2 上でエンティティテーブルを自動生成
    "spring.jpa.hibernate.ddl-auto=create-drop",
    // Flyway は PostgreSQL 専用 SQL を含むため無効化
    "spring.flyway.enabled=false",
    // Spring Batch のメタデータテーブルは H2 上で自動作成
    "spring.batch.jdbc.initialize-schema=always",
    "gbizinfo.api.base-url=http://localhost",
    "gbizinfo.api.token=test-token"
})
class GBizInfoImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    // 複数の Job bean が存在するため、テスト対象ジョブを明示的に指定する
    @Autowired
    @Qualifier(GBizInfoImportJobConfig.JOB_NAME)
    private Job importJob;

    @MockBean
    private CompanyUpsertService companyUpsertService;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(importJob);
    }

    @Test
    @DisplayName("有効な JSON ファイルを処理してジョブが COMPLETED になる")
    void importJob_completesSuccessfully() throws Exception {
        String inputFilePath = resolveTestFilePath("test-hojin.json");

        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", inputFilePath)
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    @DisplayName("有効な 2 件に対して upsert が 2 回呼ばれる（無効 2 件はフィルタされる）")
    void importJob_callsUpsertForValidRecordsOnly() throws Exception {
        String inputFilePath = resolveTestFilePath("test-hojin.json");

        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", inputFilePath)
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        jobLauncherTestUtils.launchJob(params);

        // corporate_number=null または name=null の 2 件はプロセッサがスキップ
        // 残り 2 件のみ upsert が呼ばれることを確認
        verify(companyUpsertService, times(2)).upsert(any());
    }

    @Test
    @DisplayName("processor が null を返したレコードは writeCount に含まれず filterCount に計上される")
    void importStep_processorFiltersInvalidRecords() throws Exception {
        String inputFilePath = resolveTestFilePath("test-hojin.json");

        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", inputFilePath)
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchStep(
            "gbizInfoImportStep", params);

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getWriteCount()).isEqualTo(2);
        assertThat(stepExecution.getFilterCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("空の JSON 配列を処理してジョブが COMPLETED、upsert が呼ばれない")
    void importJob_emptyFile_completesWithZeroWrites() throws Exception {
        String inputFilePath = resolveTestFilePath("test-hojin-empty.json");

        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", inputFilePath)
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verifyNoInteractions(companyUpsertService);
    }

    @Test
    @DisplayName("存在しないファイルパスを指定した場合、ジョブが FAILED になる")
    void importJob_nonExistentFile_failsJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", "/nonexistent/path/file.json")
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    @Test
    @DisplayName("upsert が DataAccessException をスローした場合、ジョブが FAILED になる（noSkip）")
    void importJob_dbError_failsJob() throws Exception {
        String inputFilePath = resolveTestFilePath("test-hojin.json");

        doThrow(new DataAccessResourceFailureException("DB connection failed"))
            .when(companyUpsertService).upsert(any());

        JobParameters params = new JobParametersBuilder()
            .addString("inputFilePath", inputFilePath)
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
    }

    private String resolveTestFilePath(String resourceName) {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertThat(resource).as("テストリソース '%s' が見つかりません", resourceName).isNotNull();
        return resource.getPath();
    }
}
