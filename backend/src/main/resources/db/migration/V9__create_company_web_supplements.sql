CREATE TABLE IF NOT EXISTS company_web_supplements (
    id               BIGSERIAL    NOT NULL,
    corporate_number VARCHAR(13)  NOT NULL UNIQUE,
    source_url       TEXT,
    description      TEXT,
    employees_count  INTEGER,
    founded_year     INTEGER,
    raw_html_path    TEXT,
    scraped_at       TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_web_supplements PRIMARY KEY (id),
    CONSTRAINT fk_company_web_supplements_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

COMMENT ON TABLE company_web_supplements IS 'Webスクレイピングによる補完情報';
