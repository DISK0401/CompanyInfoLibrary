package com.companylib.batch.infrastructure.gbizinfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * gBizINFO 日次差分更新の対象法人番号を DB で管理するリポジトリ。
 * ExecutionContext はシリアライズサイズに上限があるため、大量データは DB テーブルで管理する。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UpdateTargetRepository {

    private final JdbcTemplate jdbcTemplate;

    /** 指定 jobExecutionId の対象レコードを全件削除（冪等性確保） */
    public void clearTargets(long jobExecutionId) {
        int deleted = jdbcTemplate.update(
            "DELETE FROM gbizinfo_update_targets WHERE job_execution_id = ?",
            jobExecutionId
        );
        log.debug("対象レコード削除: jobExecutionId={}, deleted={}", jobExecutionId, deleted);
    }

    /** 法人番号リストを一括 INSERT */
    public void bulkInsert(long jobExecutionId, List<String> corporateNumbers) {
        List<Object[]> batchArgs = corporateNumbers.stream()
            .map(cn -> new Object[]{jobExecutionId, cn})
            .toList();
        jdbcTemplate.batchUpdate(
            "INSERT INTO gbizinfo_update_targets (job_execution_id, corporate_number) VALUES (?, ?)",
            batchArgs
        );
        log.info("対象法人番号登録: jobExecutionId={}, count={}", jobExecutionId, corporateNumbers.size());
    }

    /** ページング取得（chunk 処理用） */
    public List<String> fetchPage(long jobExecutionId, int offset, int limit) {
        return jdbcTemplate.queryForList(
            "SELECT corporate_number FROM gbizinfo_update_targets " +
            "WHERE job_execution_id = ? AND processed = FALSE " +
            "ORDER BY id LIMIT ? OFFSET ?",
            String.class,
            jobExecutionId, limit, offset
        );
    }

    /** 処理済みマーク */
    public void markProcessed(long jobExecutionId, List<String> corporateNumbers) {
        List<Object[]> batchArgs = corporateNumbers.stream()
            .map(cn -> new Object[]{jobExecutionId, cn})
            .toList();
        jdbcTemplate.batchUpdate(
            "UPDATE gbizinfo_update_targets SET processed = TRUE " +
            "WHERE job_execution_id = ? AND corporate_number = ?",
            batchArgs
        );
    }

    /** 未処理件数を取得 */
    public long countUnprocessed(long jobExecutionId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM gbizinfo_update_targets WHERE job_execution_id = ? AND processed = FALSE",
            Long.class,
            jobExecutionId
        );
        return count != null ? count : 0L;
    }
}
