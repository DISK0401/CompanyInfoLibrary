CREATE TABLE IF NOT EXISTS companies (
    corporate_number    VARCHAR(13)  NOT NULL,
    name                VARCHAR(500) NOT NULL,
    kana                VARCHAR(500),
    name_en             VARCHAR(500),
    postal_code         VARCHAR(10),
    location            TEXT,
    company_url         TEXT,
    business_summary    TEXT,
    capital_stock       BIGINT,
    employee_number     INTEGER,
    company_size_male   INTEGER,
    company_size_female INTEGER,
    representative_name     VARCHAR(200),
    representative_position VARCHAR(200),
    date_of_establishment   DATE,
    founding_year           INTEGER,
    status                  VARCHAR(50),
    close_date              DATE,
    close_cause             VARCHAR(200),
    gbizinfo_update_date    DATE,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_companies PRIMARY KEY (corporate_number),
    CONSTRAINT chk_corporate_number_format CHECK (corporate_number ~ '^\d{13}$')
);

CREATE INDEX IF NOT EXISTS idx_companies_name ON companies USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_companies_kana ON companies USING gin (kana gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_companies_location ON companies USING gin (location gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_companies_capital_stock ON companies (capital_stock);
CREATE INDEX IF NOT EXISTS idx_companies_employee_number ON companies (employee_number);
CREATE INDEX IF NOT EXISTS idx_companies_status ON companies (status);

COMMENT ON TABLE companies IS '法人基本情報';
COMMENT ON COLUMN companies.corporate_number IS '法人番号（13桁）';
