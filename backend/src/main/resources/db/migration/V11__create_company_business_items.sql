CREATE TABLE IF NOT EXISTS company_business_items (
    id               BIGSERIAL    NOT NULL,
    corporate_number VARCHAR(13)  NOT NULL,
    item_name        VARCHAR(500) NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_company_business_items PRIMARY KEY (id),
    CONSTRAINT fk_company_business_items_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_company_business_items_corporate_number ON company_business_items (corporate_number);

COMMENT ON TABLE company_business_items IS '営業品目（業種）';
