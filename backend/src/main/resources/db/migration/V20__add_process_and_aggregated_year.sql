-- Hojinjoho JSON に存在するが未実装だったフィールドを追加

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS process          VARCHAR(10),
    ADD COLUMN IF NOT EXISTS aggregated_year  VARCHAR(10);

COMMENT ON COLUMN companies.process         IS '処理区分コード（01=新規, 11=商号変更, 12=所在地変更, 21=閉鎖 等）';
COMMENT ON COLUMN companies.aggregated_year IS '集計年（gBizINFO 内部管理用）';
