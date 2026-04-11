package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.GBizInfoApiClient;
import com.companylib.batch.infrastructure.gbizinfo.CompanyUpsertService;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GBizInfoDailyUpdateJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GBizInfoApiClient apiClient;
    private final CompanyUpsertService companyUpsertService;

    public static final String JOB_NAME = "gbizInfoDailyUpdateJob";

    @Bean(JOB_NAME)
    public Job gbizInfoDailyUpdateJob(Step fetchUpdateInfoStep, Step updateCompaniesStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(fetchUpdateInfoStep)
            .next(updateCompaniesStep)
            .build();
    }

    /** Step1: /updateInfo API から更新法人番号リストを取得してJobExecutionContextに保存 */
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
            String targetFrom = from != null ? from
                : LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String targetTo = to != null ? to
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            log.info("差分取得期間: {} ~ {}", targetFrom, targetTo);
            UpdateInfoResponse response = apiClient.getUpdateInfo(targetFrom, targetTo);

            List<String> corporateNumbers = response.getHojinInfos().stream()
                .map(UpdateInfoResponse.UpdatedHojinInfo::getCorporateNumber)
                .toList();

            log.info("更新対象法人数: {}", corporateNumbers.size());
            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("corporateNumbers", corporateNumbers);

            return RepeatStatus.FINISHED;
        };
    }

    /** Step2: 各法人番号の詳細情報を取得して DB UPSERT */
    @Bean
    public Step updateCompaniesStep() {
        return new StepBuilder("updateCompaniesStep", jobRepository)
            .tasklet(updateCompaniesTasklet(), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public Tasklet updateCompaniesTasklet() {
        return (contribution, chunkContext) -> {
            @SuppressWarnings("unchecked")
            List<String> corporateNumbers = (List<String>) chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("corporateNumbers");

            if (corporateNumbers == null || corporateNumbers.isEmpty()) {
                log.info("更新対象なし");
                return RepeatStatus.FINISHED;
            }

            int success = 0;
            int failed = 0;
            for (String corporateNumber : corporateNumbers) {
                try {
                    HojinInfoResponse response = apiClient.getHojinInfo(corporateNumber);
                    if (response != null && response.getHojinInfos() != null) {
                        response.getHojinInfos().forEach(companyUpsertService::upsert);
                        success++;
                    }
                    // API レート制限対策（1件あたり最低100ms待機）
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("更新失敗: corporateNumber={}, error={}", corporateNumber, e.getMessage());
                    failed++;
                }
            }
            log.info("日次差分更新完了: success={}, failed={}", success, failed);
            return RepeatStatus.FINISHED;
        };
    }
}
