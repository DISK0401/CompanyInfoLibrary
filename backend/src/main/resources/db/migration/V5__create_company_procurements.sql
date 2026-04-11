CREATE TABLE IF NOT EXISTS company_procurements (
    id                 BIGSERIAL    NOT NULL,
    corporate_number   VARCHAR(13)  NOT NULL,
    title              TEXT,
    amount             BIGINT,
    date_of_order      DATE,
    government_departments VARCHAR(500),
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_procurements PRIMARY KEY (id),
    CONSTRAINT fk_company_procurements_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_procurements_corporate_number ON company_procurements (corporate_number);

COMMENT ON TABLE company_procurements IS '調達情報（政府調達契約）';
