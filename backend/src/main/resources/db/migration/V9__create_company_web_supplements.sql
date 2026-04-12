CREATE TABLE IF NOT EXISTS company_web_supplements (
    id               BIGSERIAL    NOT NULL,
    corporate_number VARCHAR(13)  NOT NULL,
    source_url       TEXT         NOT NULL,
    description      TEXT,
    employees_count  INTEGER,
    founded_year     INTEGER,
    raw_html_path    TEXT,
    scraped_at       TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_web_supplements PRIMARY KEY (id),
    CONSTRAINT uq_company_web_supplements_corp_url UNIQUE (corporate_number, source_url),
    CONSTRAINT fk_company_web_supplements_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_web_supplements_corporate_number ON company_web_supplements (corporate_number);

COMMENT ON TABLE company_web_supplements IS 'Webスクレイピングによる補完情報';
