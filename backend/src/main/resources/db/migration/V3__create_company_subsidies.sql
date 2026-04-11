CREATE TABLE IF NOT EXISTS company_subsidies (
    id                    BIGSERIAL    NOT NULL,
    corporate_number      VARCHAR(13)  NOT NULL,
    title                 TEXT,
    amount                BIGINT,
    date_of_approval      DATE,
    government_departments VARCHAR(500),
    subsidy_resource      VARCHAR(200),
    target                TEXT,
    note                  TEXT,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_subsidies PRIMARY KEY (id),
    CONSTRAINT fk_company_subsidies_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_subsidies_corporate_number ON company_subsidies (corporate_number);

COMMENT ON TABLE company_subsidies IS '補助金情報';
