-- 届出・認定情報 / 表彰情報 / 調達情報 に UPSERT 用の key_field を追加

ALTER TABLE company_certifications
    ADD COLUMN IF NOT EXISTS key_field VARCHAR(200),
    ADD CONSTRAINT uq_company_certifications_key_field UNIQUE (key_field);

ALTER TABLE company_commendations
    ADD COLUMN IF NOT EXISTS key_field VARCHAR(200),
    ADD CONSTRAINT uq_company_commendations_key_field UNIQUE (key_field);

ALTER TABLE company_procurements
    ADD COLUMN IF NOT EXISTS key_field VARCHAR(200),
    ADD CONSTRAINT uq_company_procurements_key_field UNIQUE (key_field);

ALTER TABLE company_subsidies
    ADD COLUMN IF NOT EXISTS key_field VARCHAR(200),
    ADD CONSTRAINT uq_company_subsidies_key_field UNIQUE (key_field);

ALTER TABLE company_patents
    ADD COLUMN IF NOT EXISTS key_field VARCHAR(200),
    ADD CONSTRAINT uq_company_patents_key_field UNIQUE (key_field);

COMMENT ON COLUMN company_certifications.key_field IS 'gBizINFO付番の一意識別子（UPSERT キー）';
COMMENT ON COLUMN company_commendations.key_field IS 'gBizINFO付番の一意識別子（UPSERT キー）';
COMMENT ON COLUMN company_procurements.key_field IS 'gBizINFO付番の一意識別子（UPSERT キー）';
COMMENT ON COLUMN company_subsidies.key_field IS 'gBizINFO付番の一意識別子（UPSERT キー）';
COMMENT ON COLUMN company_patents.key_field IS 'gBizINFO付番の一意識別子（UPSERT キー）';
