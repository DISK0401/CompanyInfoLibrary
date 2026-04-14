-- JSON実データ照合で判明した未実装フィールドの追加

-- ── company_finances: 経常収益を追加 ─────────────────────────────────────────
-- management_index.ordinary_income_summary_of_business_results（経常収益）は
-- ordinary_income_loss（経常利益/損失）とは別フィールドとして JSON・Swagger に存在する
ALTER TABLE company_finances
    ADD COLUMN IF NOT EXISTS ordinary_income BIGINT;

COMMENT ON COLUMN company_finances.ordinary_income IS '経常収益（円）';

-- ── company_patents: 特許分類を追加 ──────────────────────────────────────────
-- classifications は [{コード値, コード名, 日本語}] の配列として JSON に存在する
ALTER TABLE company_patents
    ADD COLUMN IF NOT EXISTS classifications JSONB;

COMMENT ON COLUMN company_patents.classifications IS '特許分類リスト（JSONB 配列: [{コード値, コード名, 日本語}]）';

-- ── company_procurements: 備考を追加 ─────────────────────────────────────────
ALTER TABLE company_procurements
    ADD COLUMN IF NOT EXISTS note TEXT;

COMMENT ON COLUMN company_procurements.note IS '備考';

-- ── company_certifications: 対象を追加 ───────────────────────────────────────
ALTER TABLE company_certifications
    ADD COLUMN IF NOT EXISTS target TEXT;

COMMENT ON COLUMN company_certifications.target IS '認定対象';

-- ── company_commendations: 備考・受賞対象を追加 ────────────────────────────────
ALTER TABLE company_commendations
    ADD COLUMN IF NOT EXISTS note TEXT,
    ADD COLUMN IF NOT EXISTS target TEXT;

COMMENT ON COLUMN company_commendations.note   IS '備考';
COMMENT ON COLUMN company_commendations.target IS '受賞対象';
