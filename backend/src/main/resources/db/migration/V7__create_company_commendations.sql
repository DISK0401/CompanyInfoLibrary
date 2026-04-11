CREATE TABLE IF NOT EXISTS company_commendations (
    id                  BIGSERIAL    NOT NULL,
    corporate_number    VARCHAR(13)  NOT NULL,
    title               TEXT,
    date_of_commendation DATE,
    government_departments VARCHAR(500),
    category            VARCHAR(200),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_commendations PRIMARY KEY (id),
    CONSTRAINT fk_company_commendations_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_commendations_corporate_number ON company_commendations (corporate_number);

COMMENT ON TABLE company_commendations IS '表彰情報';
