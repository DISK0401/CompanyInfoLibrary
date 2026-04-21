-- job_execution_id → job_instance_id に変更
-- 同じJobParametersでの再実行時に同じキーで未処理レコードを参照できるようにする
ALTER TABLE gbizinfo_update_targets
    RENAME COLUMN job_execution_id TO job_instance_id;

DROP INDEX IF EXISTS idx_gbizinfo_update_targets_job_exec;
CREATE INDEX IF NOT EXISTS idx_gbizinfo_update_targets_job_instance ON gbizinfo_update_targets (job_instance_id, processed);
