CREATE TABLE IF NOT EXISTS company_finances (
    id                      BIGSERIAL    NOT NULL,
    corporate_number        VARCHAR(13)  NOT NULL,
    period                  VARCHAR(20),
    accounting_standards    VARCHAR(50),
    net_sales               BIGINT,
    gross_operating_revenue BIGINT,
    operating_revenue1      BIGINT,
    operating_revenue2      BIGINT,
    net_income_loss         BIGINT,
    ordinary_income_loss    BIGINT,
    total_assets            BIGINT,
    net_assets              BIGINT,
    capital_stock           BIGINT,
    number_of_employees     INTEGER,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_finances PRIMARY KEY (id),
    CONSTRAINT uq_company_finances_corporate_number_period UNIQUE (corporate_number, period),
    CONSTRAINT fk_company_finances_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_finances_corporate_number_period ON company_finances (corporate_number, period DESC);

COMMENT ON TABLE company_finances IS '財務情報（年度別）';
