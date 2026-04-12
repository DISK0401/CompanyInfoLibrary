CREATE TABLE IF NOT EXISTS scraping_queue (
    id                BIGSERIAL    NOT NULL,
    corporate_number  VARCHAR(13)  NOT NULL,
    target_url        TEXT         NOT NULL,
    priority          INTEGER      NOT NULL DEFAULT 0,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count       INTEGER      NOT NULL DEFAULT 0,
    last_attempted_at TIMESTAMP,
    scraped_at        TIMESTAMP,
    error_message     TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_scraping_queue PRIMARY KEY (id),
    CONSTRAINT uq_scraping_queue_corp_url UNIQUE (corporate_number, target_url),
    CONSTRAINT chk_scraping_queue_status CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'SKIPPED')),
    CONSTRAINT fk_scraping_queue_corporate_number
        FOREIGN KEY (corporate_number) REFERENCES companies (corporate_number) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_scraping_queue_status_priority ON scraping_queue (status, priority DESC);
CREATE INDEX IF NOT EXISTS idx_scraping_queue_corporate_number ON scraping_queue (corporate_number);
CREATE INDEX IF NOT EXISTS idx_scraping_queue_pending ON scraping_queue (last_attempted_at)
    WHERE status IN ('PENDING', 'PROCESSING');

COMMENT ON TABLE scraping_queue IS 'Webスクレイピングのキュー管理テーブル';
COMMENT ON COLUMN scraping_queue.priority IS '優先度（数値が大きいほど優先）';
COMMENT ON COLUMN scraping_queue.status IS 'ステータス: PENDING/PROCESSING/SUCCESS/FAILED/SKIPPED';
