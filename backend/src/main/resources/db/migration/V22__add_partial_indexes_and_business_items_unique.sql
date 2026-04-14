-- V22: key_field 列の UNIQUE 制約を partial index に置き換え、
--      company_business_items に (corporate_number, item_name) の UNIQUE 制約を追加
--
-- 背景:
--   V13 で追加した通常の UNIQUE 制約は PostgreSQL において NULL 同士を等しくないと扱うため、
--   key_field が NULL の行が複数挿入されても制約が発火せず重複が蓄積する。
--   ON CONFLICT (key_field) も NULL 行では動作しないため、以下の partial index に置き換える。
--
--   また V21 で削除した複合 UNIQUE 制約の代替として、key_field IS NOT NULL の行だけを
--   対象とした partial index を設けることで同等の保護を維持する。

-- ── 1. 既存の通常 UNIQUE 制約を削除 ──────────────────────────────────────────

ALTER TABLE company_certifications  DROP CONSTRAINT IF EXISTS uq_company_certifications_key_field;
ALTER TABLE company_commendations   DROP CONSTRAINT IF EXISTS uq_company_commendations_key_field;
ALTER TABLE company_procurements    DROP CONSTRAINT IF EXISTS uq_company_procurements_key_field;
ALTER TABLE company_subsidies       DROP CONSTRAINT IF EXISTS uq_company_subsidies_key_field;
ALTER TABLE company_patents         DROP CONSTRAINT IF EXISTS uq_company_patents_key_field;
ALTER TABLE company_financial_statements DROP CONSTRAINT IF EXISTS uq_company_financial_statements_key_field;

-- ── 2. Partial unique index（key_field IS NOT NULL の行のみ対象） ──────────────

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_certifications_key_field
    ON company_certifications (key_field)
    WHERE key_field IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_commendations_key_field
    ON company_commendations (key_field)
    WHERE key_field IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_procurements_key_field
    ON company_procurements (key_field)
    WHERE key_field IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_subsidies_key_field
    ON company_subsidies (key_field)
    WHERE key_field IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_patents_key_field
    ON company_patents (key_field)
    WHERE key_field IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_company_financial_statements_key_field
    ON company_financial_statements (key_field)
    WHERE key_field IS NOT NULL;

-- ── 3. company_business_items: (corporate_number, item_name) の UNIQUE 制約を追加 ──

-- INSERT ... ON CONFLICT (corporate_number, item_name) DO NOTHING を使用するために必要。
-- 同一チャンク内で同一法人番号のレコードが複数あっても重複 INSERT を安全に無視できる。
ALTER TABLE company_business_items
    ADD CONSTRAINT uq_company_business_items_corp_item
        UNIQUE (corporate_number, item_name);
