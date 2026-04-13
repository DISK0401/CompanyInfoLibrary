-- gBizINFO 日次差分更新ジョブで使用する一時テーブル
-- ExecutionContext の代わりに DB でステップ間のデータを管理する
CREATE TABLE IF NOT EXISTS gbizinfo_update_targets (
    id               BIGSERIAL   NOT NULL,
    job_execution_id BIGINT      NOT NULL,
    corporate_number VARCHAR(13) NOT NULL,
    processed        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_gbizinfo_update_targets PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_gbizinfo_update_targets_job_exec ON gbizinfo_update_targets (job_execution_id, processed);

COMMENT ON TABLE gbizinfo_update_targets IS 'gBizINFO日次差分更新ジョブの対象法人番号管理テーブル';
