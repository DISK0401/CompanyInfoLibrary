-- 職場情報テーブルを gBizINFO API v2 の WorkplaceInfoBeanV2 構造に合わせて再設計

DROP TABLE IF EXISTS company_workplaces CASCADE;

CREATE TABLE company_workplaces (
    id                                      BIGSERIAL    NOT NULL,
    corporate_number                        VARCHAR(13)  NOT NULL,
    -- 勤務基本情報 (WorkplaceBaseInfosV2)
    average_age                             NUMERIC(5,1),
    average_continuous_service_years        NUMERIC(5,1),
    average_continuous_service_years_male   NUMERIC(5,1),
    average_continuous_service_years_female NUMERIC(5,1),
    average_continuous_service_years_type   VARCHAR(50),
    month_average_overtime                  NUMERIC(5,1),
    -- 女性活躍情報 (WomenActivityInfosV2)
    female_workers_proportion               NUMERIC(6,2),
    female_workers_proportion_type          VARCHAR(50),
    female_share_of_manager                 INTEGER,
    gender_total_of_manager                 INTEGER,
    female_share_of_officers                INTEGER,
    gender_total_of_officers                INTEGER,
    -- 育児・仕事両立 (CompatibilityOfChildcareAndWorkV2)
    paternity_leave_acquisition_num         INTEGER,
    number_of_paternity_leave               INTEGER,
    maternity_leave_acquisition_num         INTEGER,
    number_of_maternity_leave               INTEGER,
    -- メタデータ
    key_field                               VARCHAR(200),
    created_at                              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                              TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_workplaces PRIMARY KEY (id),
    CONSTRAINT uq_company_workplaces_corporate_number UNIQUE (corporate_number),
    CONSTRAINT fk_company_workplaces_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_workplaces_corporate_number ON company_workplaces (corporate_number);

COMMENT ON TABLE company_workplaces IS '職場情報（gBizINFO API v2 WorkplaceInfoBeanV2 準拠）';
