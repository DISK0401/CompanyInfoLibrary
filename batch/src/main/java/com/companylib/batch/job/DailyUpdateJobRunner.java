package com.companylib.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * gbizInfoDailyUpdateJob 専用の起動ハンドラ。
 *
 * <p>Spring Boot 標準の {@link JobLauncherApplicationRunner} の代わりに使用し、
 * 以下の2点を追加で対応する:
 * <ul>
 *   <li>{@code force=true} パラメータ指定時に既存の COMPLETED インスタンスを削除して強制再実行</li>
 *   <li>{@link JobInstanceAlreadyCompleteException} 発生時はアプリを落とさず WARN ログで終了</li>
 * </ul>
 *
 * <p>このクラスは {@code spring.batch.job.name=gbizInfoDailyUpdateJob} のときのみ動作する。
 * 他のジョブ名が指定された場合は何もせず、標準の {@link JobLauncherApplicationRunner} に委譲される。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DailyUpdateJobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final JdbcTemplate jdbcTemplate;

    @Qualifier(GBizInfoDailyUpdateJobConfig.JOB_NAME)
    private final Job gbizInfoDailyUpdateJob;

    @org.springframework.beans.factory.annotation.Value("${spring.batch.job.enabled:false}")
    private boolean jobEnabled;

    @org.springframework.beans.factory.annotation.Value("${spring.batch.job.name:}")
    private String jobName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!jobEnabled || !GBizInfoDailyUpdateJobConfig.JOB_NAME.equals(jobName)) {
            return;
        }

        boolean force = args.containsOption("force") &&
            "true".equalsIgnoreCase(args.getOptionValues("force").get(0));

        String from = getArg(args, "from");
        String to   = getArg(args, "to");

        JobParametersBuilder builder = new JobParametersBuilder();
        if (from != null) builder.addString("from", from);
        if (to   != null) builder.addString("to",   to);
        JobParameters params = builder.toJobParameters();

        if (force) {
            deleteCompletedInstance(params);
        }

        try {
            JobExecution execution = jobLauncher.run(gbizInfoDailyUpdateJob, params);
            if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("ジョブ完了: status={}", execution.getStatus());
            } else {
                log.warn("ジョブが正常完了しませんでした: status={}", execution.getStatus());
            }
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("同一パラメータのジョブが既に完了しています。再実行する場合は force=true を指定してください。" +
                " (from={}, to={})", from, to);
        }
    }

    /**
     * 指定パラメータに対応する COMPLETED ジョブインスタンスのメタデータを削除する。
     * force=true 時に呼び出し、同じパラメータで再実行できるようにする。
     */
    private void deleteCompletedInstance(JobParameters params) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(
            GBizInfoDailyUpdateJobConfig.JOB_NAME, 0, Integer.MAX_VALUE);

        for (JobInstance instance : instances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            boolean hasCompleted = executions.stream()
                .anyMatch(e -> e.getStatus() == BatchStatus.COMPLETED);
            boolean paramsMatch = executions.stream()
                .anyMatch(e -> e.getJobParameters().equals(params));

            if (hasCompleted && paramsMatch) {
                log.info("force=true: 完了済みジョブインスタンスを削除します (instanceId={})", instance.getInstanceId());
                deleteInstanceMetadata(instance.getInstanceId());
                jdbcTemplate.update(
                    "DELETE FROM gbizinfo_update_targets WHERE job_instance_id = ?",
                    instance.getInstanceId()
                );
                return;
            }
        }
    }

    private void deleteInstanceMetadata(long instanceId) {
        jdbcTemplate.update(
            "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID IN " +
            "(SELECT se.STEP_EXECUTION_ID FROM BATCH_STEP_EXECUTION se " +
            " JOIN BATCH_JOB_EXECUTION je ON se.JOB_EXECUTION_ID = je.JOB_EXECUTION_ID " +
            " WHERE je.JOB_INSTANCE_ID = ?)", instanceId);

        jdbcTemplate.update(
            "DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN " +
            "(SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID = ?)", instanceId);

        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN " +
            "(SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID = ?)", instanceId);

        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID IN " +
            "(SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID = ?)", instanceId);

        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID = ?", instanceId);

        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?", instanceId);
    }

    private String getArg(ApplicationArguments args, String name) {
        if (!args.containsOption(name)) return null;
        List<String> values = args.getOptionValues(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
