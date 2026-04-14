-- 1. companies: 法人レベルのメタデータ（データ品質・出典元・取込頻度等）
ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS meta_data_json JSONB;

COMMENT ON COLUMN companies.meta_data_json IS '法人レベルのメタデータ（gBizINFO: key_field, data_quality, source, import_frequency, last_acquisition_date, last_update_date）';

-- 2. company_patents: 文献固定アドレス（現在は常に null だが Swagger 定義あり）
ALTER TABLE company_patents
    ADD COLUMN IF NOT EXISTS url TEXT;

COMMENT ON COLUMN company_patents.url IS '文献固定アドレス（J-PlatPat リンク。将来的に提供予定）';

-- 3. company_finances: Finance レベルのメタデータ key_field + 全 unit_ref + net_premiums_written
ALTER TABLE company_finances
    ADD COLUMN IF NOT EXISTS finance_key_field              VARCHAR(200),
    ADD COLUMN IF NOT EXISTS net_sales_unit                 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS operating_revenue1_unit        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS operating_revenue2_unit        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS gross_operating_revenue_unit   VARCHAR(20),
    ADD COLUMN IF NOT EXISTS ordinary_income_unit           VARCHAR(20),
    ADD COLUMN IF NOT EXISTS ordinary_income_loss_unit      VARCHAR(20),
    ADD COLUMN IF NOT EXISTS net_income_loss_unit           VARCHAR(20),
    ADD COLUMN IF NOT EXISTS capital_stock_unit             VARCHAR(20),
    ADD COLUMN IF NOT EXISTS net_assets_unit                VARCHAR(20),
    ADD COLUMN IF NOT EXISTS total_assets_unit              VARCHAR(20),
    ADD COLUMN IF NOT EXISTS number_of_employees_unit       VARCHAR(20),
    ADD COLUMN IF NOT EXISTS net_premiums_written           BIGINT,
    ADD COLUMN IF NOT EXISTS net_premiums_written_unit      VARCHAR(20);

COMMENT ON COLUMN company_finances.finance_key_field            IS 'Finance メタデータの gBizINFO key_field';
COMMENT ON COLUMN company_finances.net_sales_unit               IS '売上高の単位（JPY 等）';
COMMENT ON COLUMN company_finances.operating_revenue1_unit      IS '営業収益の単位';
COMMENT ON COLUMN company_finances.operating_revenue2_unit      IS '営業収入の単位';
COMMENT ON COLUMN company_finances.gross_operating_revenue_unit IS '営業総収入の単位';
COMMENT ON COLUMN company_finances.ordinary_income_unit         IS '経常収益の単位';
COMMENT ON COLUMN company_finances.ordinary_income_loss_unit    IS '経常利益又は経常損失の単位';
COMMENT ON COLUMN company_finances.net_income_loss_unit         IS '当期純利益又は当期純損失の単位';
COMMENT ON COLUMN company_finances.capital_stock_unit           IS '資本金の単位';
COMMENT ON COLUMN company_finances.net_assets_unit              IS '純資産額の単位';
COMMENT ON COLUMN company_finances.total_assets_unit            IS '総資産額の単位';
COMMENT ON COLUMN company_finances.number_of_employees_unit     IS '従業員数の単位（pure 等）';
COMMENT ON COLUMN company_finances.net_premiums_written         IS '正味収入保険料（円）。保険会社のみ、通常は null';
COMMENT ON COLUMN company_finances.net_premiums_written_unit    IS '正味収入保険料の単位';
