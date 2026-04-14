package com.companylib.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    @org.springframework.beans.factory.annotation.Qualifier(GBizInfoDailyUpdateJobConfig.JOB_NAME)
    private final Job gbizInfoDailyUpdateJob;

    /** 毎日 02:00 に日次差分更新ジョブを実行 */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Tokyo")
    public void runDailyUpdate() {
        try {
            log.info("日次差分更新ジョブ 開始");
            JobParameters params = new JobParametersBuilder()
                .addString("runAt", LocalDateTime.now().toString())
                .toJobParameters();
            jobLauncher.run(gbizInfoDailyUpdateJob, params);
        } catch (Exception e) {
            log.error("日次差分更新ジョブ 失敗", e);
        }
    }
}
