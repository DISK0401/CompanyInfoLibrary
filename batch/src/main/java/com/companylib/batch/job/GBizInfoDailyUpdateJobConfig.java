package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.GBizInfoApiClient;
import com.companylib.batch.infrastructure.gbizinfo.CompanyFullUpsertService;
import com.companylib.batch.infrastructure.gbizinfo.UpdateTargetRepository;
import com.companylib.batch.infrastructure.gbizinfo.UpdateTargetRepository.IdAndJson;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import com.companylib.batch.infrastructure.gbizinfo.dto.UpdateInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 *   <li>fetchUpdateInfoStep — /updateInfo API から全ページ取得し、法人情報 JSON を DB に保存</li>
 *   <li>updateCompaniesStep — DB の JSON を読み出して全テーブルを UPSERT（API 再取得なし）</li>
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
    private final ObjectMapper objectMapper;

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

    // ── Step 1: 更新対象法人情報 JSON 取得・保存 ──────────────────────────────

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
            long jobInstanceId = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getJobInstance().getInstanceId();

            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String targetFrom = from != null ? from : yesterday;
            String targetTo = to != null ? to : yesterday;

            log.info("=== 差分取得期間: {} ~ {} ===", targetFrom, targetTo);

            // 冪等性: 前回の残骸を削除してから再登録
            updateTargetRepository.clearTargets(jobInstanceId);

            // ページングで全件取得し JSON のまま DB に保存
            int page = 1;
            int totalPage = 1;
            int totalRegistered = 0;

            while (page <= totalPage) {
                UpdateInfoResponse response = apiClient.getUpdateInfo(targetFrom, targetTo, page);

                if (response == null || response.getHojinInfos() == null || response.getHojinInfos().isEmpty()) {
                    if (page == 1) {
                        log.info("更新対象なし");
                    }
                    break;
                }

                // 初回レスポンスで総ページ数を確定
                if (page == 1 && response.getTotalPage() != null) {
                    totalPage = response.getTotalPage();
                    int total = response.getTotalCount() != null ? response.getTotalCount() : 0;
                    log.info("取得開始: 総件数={}, 総ページ数={}", total, totalPage);
                }

                // HojinInfo を JSON 文字列にシリアライズして保存
                List<String> jsonList = new ArrayList<>();
                for (HojinInfo info : response.getHojinInfos()) {
                    jsonList.add(objectMapper.writeValueAsString(info));
                }

                updateTargetRepository.bulkInsertJson(jobInstanceId, jsonList);
                totalRegistered += jsonList.size();

                log.info("updateInfo 取得進捗: {} / {} ページ [{}]",
                    page, totalPage, progressBar(page, totalPage));

                page++;
            }

            log.info("=== 更新対象法人情報 登録完了: {}件 ===", totalRegistered);
            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: DB の JSON を読み出して UPSERT ────────────────────────────────

    @Bean
    public Step updateCompaniesStep() {
        return new StepBuilder("updateCompaniesStep", jobRepository)
            .<IdAndJson, IdAndJson>chunk(CHUNK_SIZE, transactionManager)
            .reader(updateTargetReader(null))
            .writer(updateTargetWriter(null))
            .faultTolerant()
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
    public ItemReader<IdAndJson> updateTargetReader(
        @Value("#{stepExecution.jobExecution.jobInstance.instanceId}") Long jobInstanceId
    ) {
        return new ItemReader<>() {
            private List<IdAndJson> buffer = new ArrayList<>();
            private int bufferIndex = 0;
            private boolean exhausted = false;
            private long totalCount = -1;
            private long processedCount = 0;

            @Override
            public IdAndJson read() {
                if (exhausted) return null;

                if (bufferIndex >= buffer.size()) {
                    buffer = updateTargetRepository.fetchUnprocessedIdAndJsonPage(jobInstanceId, CHUNK_SIZE);
                    bufferIndex = 0;

                    if (buffer.isEmpty()) {
                        exhausted = true;
                        return null;
                    }

                    if (totalCount < 0) {
                        totalCount = updateTargetRepository.countAll(jobInstanceId);
                        log.info("=== 企業データ更新 開始: 対象{}件 ===", totalCount);
                    }
                }

                IdAndJson item = buffer.get(bufferIndex++);
                processedCount++;

                if (totalCount > 0 && processedCount % CHUNK_SIZE == 0) {
                    log.info("企業データ更新進捗: {}/{} 件 [{}]",
                        processedCount, totalCount,
                        progressBar(processedCount, totalCount));
                }

                return item;
            }
        };
    }

    @Bean
    @StepScope
    public ItemWriter<IdAndJson> updateTargetWriter(
        @Value("#{stepExecution.jobExecution.jobInstance.instanceId}") Long jobInstanceId
    ) {
        return items -> {
            List<Long> processedIds = new ArrayList<>();
            for (IdAndJson item : items) {
                try {
                    HojinInfo info = objectMapper.readValue(item.json(), HojinInfo.class);
                    companyFullUpsertService.upsert(info);
                    processedIds.add(item.id());
                } catch (Exception e) {
                    log.warn("UPSERT スキップ: id={}, reason={}", item.id(), e.getMessage());
                    processedIds.add(item.id());
                }
            }
            updateTargetRepository.markProcessedByIds(processedIds);
            log.debug("{}件 UPSERT・処理済みマーク完了", items.size());
        };
    }

    /** シンプルなプログレスバー文字列を生成する（例: "[####------] 50%"） */
    private String progressBar(long current, long total) {
        if (total <= 0) return "N/A";
        int percent = (int) (current * 100L / total);
        int filled = percent / 10;
        String bar = "[" + "#".repeat(filled) + "-".repeat(10 - filled) + "]";
        return bar + " " + percent + "%";
    }
}
