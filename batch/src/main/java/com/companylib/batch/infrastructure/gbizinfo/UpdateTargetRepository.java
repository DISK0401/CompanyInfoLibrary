package com.companylib.batch.infrastructure.gbizinfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * gBizINFO 日次差分更新の対象法人データを DB で管理するリポジトリ。
 * Step1 で取得した API レスポンス JSON をそのまま保存し、
 * Step2 で API 再取得なしに UPSERT できるようにする。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UpdateTargetRepository {

    private final JdbcTemplate jdbcTemplate;

    /** 指定 jobInstanceId の対象レコードを全件削除（冪等性確保） */
    public void clearTargets(long jobInstanceId) {
        int deleted = jdbcTemplate.update(
            "DELETE FROM gbizinfo_update_targets WHERE job_instance_id = ?",
            jobInstanceId
        );
        log.debug("対象レコード削除: jobInstanceId={}, deleted={}", jobInstanceId, deleted);
    }

    /** 法人情報 JSON リストを一括 INSERT */
    public void bulkInsertJson(long jobInstanceId, List<String> hojinInfoJsonList) {
        List<Object[]> batchArgs = hojinInfoJsonList.stream()
            .map(json -> new Object[]{jobInstanceId, json})
            .toList();
        jdbcTemplate.batchUpdate(
            "INSERT INTO gbizinfo_update_targets (job_instance_id, hojin_info_json) VALUES (?, ?::jsonb)",
            batchArgs
        );
        log.info("対象法人JSON登録: jobInstanceId={}, count={}", jobInstanceId, hojinInfoJsonList.size());
    }

    /**
     * 未処理レコードの JSON を先頭から limit 件取得（chunk 処理用）。
     * OFFSET を使わず「未処理の先頭 N 件」を繰り返し取得することで、
     * 処理済みマーク後も正しくページングが進む。
     */
    public List<String> fetchUnprocessedJsonPage(long jobInstanceId, int limit) {
        return jdbcTemplate.queryForList(
            "SELECT hojin_info_json::text FROM gbizinfo_update_targets " +
            "WHERE job_instance_id = ? AND processed = FALSE " +
            "ORDER BY id LIMIT ?",
            String.class,
            jobInstanceId, limit
        );
    }

    /** 処理済みマーク（id で特定するため id のリストを受け取る） */
    public void markProcessedByIds(List<Long> ids) {
        List<Object[]> batchArgs = ids.stream()
            .map(id -> new Object[]{id})
            .toList();
        jdbcTemplate.batchUpdate(
            "UPDATE gbizinfo_update_targets SET processed = TRUE WHERE id = ?",
            batchArgs
        );
    }

    /** 未処理レコードの id と JSON を先頭から limit 件取得 */
    public List<IdAndJson> fetchUnprocessedIdAndJsonPage(long jobInstanceId, int limit) {
        return jdbcTemplate.query(
            "SELECT id, hojin_info_json::text FROM gbizinfo_update_targets " +
            "WHERE job_instance_id = ? AND processed = FALSE " +
            "ORDER BY id LIMIT ?",
            (rs, rowNum) -> new IdAndJson(rs.getLong("id"), rs.getString("hojin_info_json")),
            jobInstanceId, limit
        );
    }

    /** 総件数を取得 */
    public long countAll(long jobInstanceId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM gbizinfo_update_targets WHERE job_instance_id = ?",
            Long.class,
            jobInstanceId
        );
        return count != null ? count : 0L;
    }

    /** 未処理件数を取得 */
    public long countUnprocessed(long jobInstanceId) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM gbizinfo_update_targets WHERE job_instance_id = ? AND processed = FALSE",
            Long.class,
            jobInstanceId
        );
        return count != null ? count : 0L;
    }

    public record IdAndJson(long id, String json) {}
}
