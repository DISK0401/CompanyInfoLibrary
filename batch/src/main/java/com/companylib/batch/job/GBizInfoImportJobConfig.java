package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.CompanyUpsertService;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GBizInfoImportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CompanyUpsertService companyUpsertService;

    public static final String JOB_NAME = "gbizInfoImportJob";
    private static final int CHUNK_SIZE = 1000;

    @Bean(JOB_NAME)
    public Job gbizInfoImportJob(Step gbizInfoImportStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(gbizInfoImportStep)
            .build();
    }

    @Bean
    public Step gbizInfoImportStep(
        JsonItemReader<HojinInfo> gbizInfoJsonReader
    ) {
        return new StepBuilder("gbizInfoImportStep", jobRepository)
            .<HojinInfo, HojinInfo>chunk(CHUNK_SIZE, transactionManager)
            .reader(gbizInfoJsonReader)
            .processor(gbizInfoImportProcessor())
            .writer(gbizInfoImportWriter())
            .faultTolerant()
            .skipLimit(1000)
            .skip(com.fasterxml.jackson.core.JsonProcessingException.class)
            .skip(IllegalArgumentException.class)
            .noSkip(org.springframework.dao.DataAccessException.class)
            .build();
    }

    @Bean
    @StepScope
    public JsonItemReader<HojinInfo> gbizInfoJsonReader(
        @Value("#{jobParameters['inputFilePath']}") String inputFilePath
    ) {
        return new JsonItemReaderBuilder<HojinInfo>()
            .name("gbizInfoJsonReader")
            .resource(new FileSystemResource(inputFilePath))
            .jsonObjectReader(new JacksonJsonObjectReader<>(HojinInfo.class))
            .build();
    }

    @Bean
    public ItemProcessor<HojinInfo, HojinInfo> gbizInfoImportProcessor() {
        return item -> {
            if (item.getCorporateNumber() == null || item.getName() == null) {
                log.warn("必須フィールド欠損のためスキップ: {}", item);
                return null;
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<HojinInfo> gbizInfoImportWriter() {
        return items -> {
            for (HojinInfo item : items) {
                companyUpsertService.upsert(item);
            }
        };
    }
}
