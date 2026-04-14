-- 実データ確認で判明した未実装フィールドの追加

-- ── companies テーブル ────────────────────────────────────────────────────────
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS kind               VARCHAR(10),
    ADD COLUMN IF NOT EXISTS industry           JSONB,
    ADD COLUMN IF NOT EXISTS qualification_grade VARCHAR(200);

COMMENT ON COLUMN companies.kind               IS '法人種別コード（301=株式会社, 302=有限会社, 305=合同会社 等）';
COMMENT ON COLUMN companies.industry           IS '業種コードリスト（日本標準産業分類の大分類コード, JSONB 配列）';
COMMENT ON COLUMN companies.qualification_grade IS '全省庁統一資格の資格等級（物品製造/販売/役務/買受の順にカンマ区切り）';

-- ── company_finances テーブル ─────────────────────────────────────────────────
ALTER TABLE company_finances
    ADD COLUMN IF NOT EXISTS fiscal_year_cover_page VARCHAR(200),
    ADD COLUMN IF NOT EXISTS major_shareholders      JSONB;

COMMENT ON COLUMN company_finances.fiscal_year_cover_page IS '事業年度（例: 第126期（自 2025年1月1日 至 2025年12月31日））';
COMMENT ON COLUMN company_finances.major_shareholders     IS '大株主情報（JSONB 配列: [{name_major_shareholders, shareholding_ratio}]）';
