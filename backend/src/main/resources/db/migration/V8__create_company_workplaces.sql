CREATE TABLE IF NOT EXISTS company_workplaces (
    id                       BIGSERIAL    NOT NULL,
    corporate_number         VARCHAR(13)  NOT NULL,
    fiscal_year              INTEGER,
    average_age              NUMERIC(5,1),
    female_workers_ratio     NUMERIC(5,2),
    month_average_overtime   NUMERIC(5,1),
    paid_vacation_consumption_rate NUMERIC(5,2),
    male_childcare_leave_rate      NUMERIC(5,2),
    female_childcare_leave_rate    NUMERIC(5,2),
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_workplaces PRIMARY KEY (id),
    CONSTRAINT uq_company_workplaces_corp_year UNIQUE (corporate_number, fiscal_year),
    CONSTRAINT fk_company_workplaces_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_workplaces_corporate_number ON company_workplaces (corporate_number);

COMMENT ON TABLE company_workplaces IS '職場情報';
