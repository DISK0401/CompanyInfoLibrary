-- corporate_number カラムを hojin_info_json JSONB に置き換える
-- Step1 で取得した API レスポンスの全データを JSON のまま保存し、
-- Step2 で API 再取得なしに UPSERT できるようにする
ALTER TABLE gbizinfo_update_targets
    DROP COLUMN corporate_number,
    ADD COLUMN corporate_number VARCHAR(13),
    ADD COLUMN hojin_info_json JSONB NOT NULL DEFAULT '{}';

-- DEFAULT は一時的なもの。既存レコードがある場合は truncate してから migrate すること
ALTER TABLE gbizinfo_update_targets ALTER COLUMN hojin_info_json DROP DEFAULT;
