CREATE TABLE IF NOT EXISTS company_patents (
    id               BIGSERIAL    NOT NULL,
    corporate_number VARCHAR(13)  NOT NULL,
    patent_type      VARCHAR(100),
    title            TEXT,
    date_of_patent   DATE,
    patent_number    VARCHAR(100),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_patents PRIMARY KEY (id),
    CONSTRAINT uq_company_patents_corp_patent_number UNIQUE (corporate_number, patent_number),
    CONSTRAINT fk_company_patents_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_patents_corporate_number ON company_patents (corporate_number);

COMMENT ON TABLE company_patents IS '特許情報';
