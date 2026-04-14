package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.GBizInfoApiClient;
import com.companylib.batch.infrastructure.gbizinfo.CompanyFullUpsertService;
import com.companylib.batch.infrastructure.gbizinfo.UpdateTargetRepository;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfoResponse;
import com.companylib.batch.infrastructure.gbizinfo.dto.UpdateInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * gBizINFO 日次差分更新ジョブ。
 *
 * <p>処理フロー:
 * <ol>
 *   <li>fetchUpdateInfoStep — /updateInfo API から更新法人番号リストを取得して DB に保存</li>
 *   <li>updateCompaniesStep — 各法人番号の詳細情報を API から取得し全テーブルを UPSERT</li>
 * </ol>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GBizInfoDailyUpdateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GBizInfoApiClient apiClient;
    private final CompanyFullUpsertService companyFullUpsertService;
    private final UpdateTargetRepository updateTargetRepository;

    public static final String JOB_NAME = "gbizInfoDailyUpdateJob";
    private static final int CHUNK_SIZE = 50;

    // ── Job ──────────────────────────────────────────────────────────────────

    @Bean(JOB_NAME)
    public Job gbizInfoDailyUpdateJob(Step fetchUpdateInfoStep, Step updateCompaniesStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(fetchUpdateInfoStep)
            .next(updateCompaniesStep)
            .build();
    }

    // ── Step 1: 更新対象法人番号リスト取得 ────────────────────────────────────

    @Bean
    public Step fetchUpdateInfoStep() {
        return new StepBuilder("fetchUpdateInfoStep", jobRepository)
            .tasklet(fetchUpdateInfoTasklet(null, null), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet fetchUpdateInfoTasklet(
        @Value("#{jobParameters['from'] ?: null}") String from,
        @Value("#{jobParameters['to'] ?: null}") String to
    ) {
        return (contribution, chunkContext) -> {
            long jobExecutionId = chunkContext.getStepContext()
                .getStepExecution().getJobExecutionId();

            String targetFrom = from != null ? from
                : LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String targetTo = to != null ? to
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            log.info("=== 差分取得期間: {} ~ {} ===", targetFrom, targetTo);

            // 冪等性: 前回の残骸を削除してから再登録
            updateTargetRepository.clearTargets(jobExecutionId);

            UpdateInfoResponse response = apiClient.getUpdateInfo(targetFrom, targetTo);
            if (response == null || response.getHojinInfos() == null || response.getHojinInfos().isEmpty()) {
                log.info("更新対象なし");
                return RepeatStatus.FINISHED;
            }

            List<String> corporateNumbers = response.getHojinInfos().stream()
                .map(UpdateInfoResponse.UpdatedHojinInfo::getCorporateNumber)
                .toList();

            updateTargetRepository.bulkInsert(jobExecutionId, corporateNumbers);
            log.info("更新対象法人数: {}", corporateNumbers.size());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: 各法人の全データを取得して UPSERT ─────────────────────────────

    @Bean
    public Step updateCompaniesStep() {
        return new StepBuilder("updateCompaniesStep", jobRepository)
            .<String, HojinInfo>chunk(CHUNK_SIZE, transactionManager)
            .reader(updateTargetReader(0L))
            .processor(updateTargetProcessor())
            .writer(updateTargetWriter(null))
            .faultTolerant()
            .retry(RuntimeException.class)
            .retryLimit(3)
            .skipLimit(100)
            .skip(IllegalArgumentException.class)
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("=== 企業データ更新ステップ開始 ===");
                }
                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("=== 企業データ更新ステップ完了: 処理={}, スキップ={} ===",
                        stepExecution.getWriteCount(), stepExecution.getSkipCount());
                    return stepExecution.getExitStatus();
                }
            })
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> updateTargetReader(
        @Value("#{stepExecution.jobExecution.id}") Long jobExecutionId
    ) {
        return new ItemReader<>() {
            private int offset = 0;
            private List<String> buffer = new ArrayList<>();
            private int bufferIndex = 0;
            private boolean exhausted = false;

            @Override
            public String read() {
                if (exhausted) return null;

                if (bufferIndex >= buffer.size()) {
                    buffer = updateTargetRepository.fetchPage(jobExecutionId, offset, CHUNK_SIZE);
                    offset += buffer.size();
                    bufferIndex = 0;

                    if (buffer.isEmpty()) {
                        exhausted = true;
                        return null;
                    }
                }

                return buffer.get(bufferIndex++);
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<String, HojinInfo> updateTargetProcessor() {
        return corporateNumber -> {
            HojinInfoResponse response = apiClient.getHojinInfo(corporateNumber);
            if (response == null || response.getHojinInfos() == null || response.getHojinInfos().isEmpty()) {
                log.warn("法人情報取得結果なし: corporateNumber={}", corporateNumber);
                return null;
            }
            return response.getHojinInfos().get(0);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<HojinInfo> updateTargetWriter(
        @Value("#{stepExecution.jobExecution.id}") Long jobExecutionId
    ) {
        return items -> {
            List<String> processedNumbers = new ArrayList<>();
            for (HojinInfo item : items) {
                companyFullUpsertService.upsert(item);
                processedNumbers.add(item.getCorporateNumber());
            }
            updateTargetRepository.markProcessed(jobExecutionId, processedNumbers);
            log.debug("{}件 UPSERT・処理済みマーク完了", items.size());
        };
    }
}
