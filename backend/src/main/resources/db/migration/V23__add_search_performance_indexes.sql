-- V23: 検索パフォーマンス改善
--
-- 問題:
--   1. CONTAINS/SUFFIX 検索で LOWER(col) LIKE LOWER('%val%') と書いていると
--      pg_trgm の GIN インデックスが使われず逐次スキャンになる。
--      PostgreSQL の pg_trgm は ILIKE または LIKE + lower() の関数インデックスに対応。
--
--   2. よく使われるソート・フィルタ列（founding_year, date_of_establishment, kind）に
--      B-tree インデックスがなく、ソート時に逐次スキャンが走る。
--
--   3. capital_stock / employee_number の NULL LAST ソートを高速化するための
--      partial index（NULL を除いたインデックス）が不足。

-- ── 1. LOWER() 関数インデックス（ILIKE 互換 / CONTAINS・SUFFIX に対応） ───────────

-- name: LOWER(name) に GIN インデックスを追加（既存の gin(name) は LIKE 前方一致のみ有効）
CREATE INDEX IF NOT EXISTS idx_companies_name_lower
    ON companies USING gin (lower(name) gin_trgm_ops);

-- kana: 同上
CREATE INDEX IF NOT EXISTS idx_companies_kana_lower
    ON companies USING gin (lower(kana) gin_trgm_ops);

-- location: 同上
CREATE INDEX IF NOT EXISTS idx_companies_location_lower
    ON companies USING gin (lower(location) gin_trgm_ops);

-- representative_name: よく使われる検索対象
CREATE INDEX IF NOT EXISTS idx_companies_representative_name_lower
    ON companies USING gin (lower(representative_name) gin_trgm_ops);

-- ── 2. よく使われるソート・フィルタ列の B-tree インデックス追加 ──────────────────

-- 創業年（昇順・降順ソートで使用）
CREATE INDEX IF NOT EXISTS idx_companies_founding_year
    ON companies (founding_year DESC NULLS LAST);

-- 設立年月日
CREATE INDEX IF NOT EXISTS idx_companies_date_of_establishment
    ON companies (date_of_establishment DESC NULLS LAST);

-- 法人種別（フィルタで使用）
CREATE INDEX IF NOT EXISTS idx_companies_kind
    ON companies (kind)
    WHERE kind IS NOT NULL;

-- ── 3. NULL LAST ソート高速化のための Partial Index ────────────────────────────

-- capital_stock: NULL を持つ行が多い場合、NULL を除いたインデックスでソートを高速化
CREATE INDEX IF NOT EXISTS idx_companies_capital_stock_not_null
    ON companies (capital_stock DESC)
    WHERE capital_stock IS NOT NULL;

-- employee_number: 同上
CREATE INDEX IF NOT EXISTS idx_companies_employee_number_not_null
    ON companies (employee_number DESC)
    WHERE employee_number IS NOT NULL;
