-- 決算情報（Kessanjoho XML）テーブル
-- 官報ベースの貸借対照表・損益計算書の要旨を格納

CREATE TABLE IF NOT EXISTS company_financial_statements (
    id               BIGSERIAL    NOT NULL,
    corporate_number VARCHAR(13)  NOT NULL,
    period           VARCHAR(100),                   -- "第XX期決算公告"
    release_date     VARCHAR(50),                    -- "YYYY年M月D日"（官報掲載日）
    unit             VARCHAR(50),                    -- "単位：千円" 等
    status           VARCHAR(20),                    -- Add / Replace
    report_data      JSONB        NOT NULL,           -- 報告書の行項目 [{reportName, asOfDate, divisions:[{name, items:[{subject,amount}]}]}]
    key_field        VARCHAR(200),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_financial_statements PRIMARY KEY (id),
    CONSTRAINT uq_company_financial_statements_key_field UNIQUE (key_field),
    CONSTRAINT fk_company_financial_statements_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_financial_statements_corporate_number ON company_financial_statements (corporate_number);
CREATE INDEX IF NOT EXISTS idx_company_financial_statements_period ON company_financial_statements (corporate_number, period);

COMMENT ON TABLE company_financial_statements IS '決算情報（官報掲載の貸借対照表・損益計算書要旨）';
COMMENT ON COLUMN company_financial_statements.report_data IS 'JSON: [{reportName, asOfDate, divisions:[{name, items:[{subject,amount}]}]}]';
