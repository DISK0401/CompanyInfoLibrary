CREATE TABLE IF NOT EXISTS company_certifications (
    id                  BIGSERIAL    NOT NULL,
    corporate_number    VARCHAR(13)  NOT NULL,
    title               TEXT,
    category            VARCHAR(200),
    date_of_approval    DATE,
    expire_date         DATE,
    government_departments VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_certifications PRIMARY KEY (id),
    CONSTRAINT fk_company_certifications_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_certifications_corporate_number ON company_certifications (corporate_number);

COMMENT ON TABLE company_certifications IS '届出・認定情報';
