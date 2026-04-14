-- 全テーブルのカラムに論理名コメントを追加

-- ── companies（法人基本情報） ────────────────────────────────────────────────
COMMENT ON COLUMN companies.corporate_number         IS '法人番号（13桁）';
COMMENT ON COLUMN companies.name                     IS '商号または名称';
COMMENT ON COLUMN companies.kana                     IS '商号または名称（カナ）';
COMMENT ON COLUMN companies.name_en                  IS '商号または名称（英字）';
COMMENT ON COLUMN companies.postal_code              IS '郵便番号';
COMMENT ON COLUMN companies.location                 IS '登記住所（本社所在地）';
COMMENT ON COLUMN companies.company_url              IS '企業WebサイトURL';
COMMENT ON COLUMN companies.business_summary         IS '事業概要';
COMMENT ON COLUMN companies.capital_stock            IS '資本金（円）';
COMMENT ON COLUMN companies.employee_number          IS '従業員数（全体）';
COMMENT ON COLUMN companies.company_size_male        IS '従業員数（男性）';
COMMENT ON COLUMN companies.company_size_female      IS '従業員数（女性）';
COMMENT ON COLUMN companies.representative_name      IS '代表者名称（役職氏名）';
COMMENT ON COLUMN companies.date_of_establishment    IS '設立年月日';
COMMENT ON COLUMN companies.founding_year            IS '創業年';
COMMENT ON COLUMN companies.status                   IS '法人ステータス（処理区分コード）';
COMMENT ON COLUMN companies.close_date               IS '登記記録の閉鎖等年月日';
COMMENT ON COLUMN companies.close_cause              IS '登記記録の閉鎖等の事由';
COMMENT ON COLUMN companies.gbizinfo_update_date     IS 'gBizINFO 最終更新日';
COMMENT ON COLUMN companies.created_at               IS '作成日時';
COMMENT ON COLUMN companies.updated_at               IS '更新日時';

-- ── company_business_items（営業品目） ───────────────────────────────────────
COMMENT ON TABLE  company_business_items             IS '営業品目（業種・日本標準産業分類）';
COMMENT ON COLUMN company_business_items.id          IS 'サロゲートキー';
COMMENT ON COLUMN company_business_items.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_business_items.item_name   IS '営業品目名（日本標準産業分類コード/名称）';
COMMENT ON COLUMN company_business_items.created_at  IS '作成日時';

-- ── company_finances（財務情報） ─────────────────────────────────────────────
COMMENT ON TABLE  company_finances                   IS '財務情報（年度別）';
COMMENT ON COLUMN company_finances.id                IS 'サロゲートキー';
COMMENT ON COLUMN company_finances.corporate_number  IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_finances.period            IS '回次（第XX期）';
COMMENT ON COLUMN company_finances.accounting_standards IS '会計基準';
COMMENT ON COLUMN company_finances.net_sales         IS '売上高（円）';
COMMENT ON COLUMN company_finances.gross_operating_revenue IS '営業総収入（円）';
COMMENT ON COLUMN company_finances.operating_revenue1 IS '営業収益（円）';
COMMENT ON COLUMN company_finances.operating_revenue2 IS '営業収入（円）';
COMMENT ON COLUMN company_finances.net_income_loss   IS '当期純利益または当期純損失（円）';
COMMENT ON COLUMN company_finances.ordinary_income_loss IS '経常利益または経常損失（円）';
COMMENT ON COLUMN company_finances.total_assets      IS '総資産額（円）';
COMMENT ON COLUMN company_finances.net_assets        IS '純資産額（円）';
COMMENT ON COLUMN company_finances.capital_stock     IS '資本金（円）';
COMMENT ON COLUMN company_finances.number_of_employees IS '従業員数';
COMMENT ON COLUMN company_finances.created_at        IS '作成日時';
COMMENT ON COLUMN company_finances.updated_at        IS '更新日時';
-- V17/V18 で追加したカラムのコメントは各マイグレーションファイルに記載

-- ── company_subsidies（補助金情報） ─────────────────────────────────────────
COMMENT ON TABLE  company_subsidies                  IS '補助金情報';
COMMENT ON COLUMN company_subsidies.id               IS 'サロゲートキー';
COMMENT ON COLUMN company_subsidies.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_subsidies.title            IS '補助金等の名称';
COMMENT ON COLUMN company_subsidies.amount           IS '交付金額（円）';
COMMENT ON COLUMN company_subsidies.date_of_approval IS '認定日（証明日）';
COMMENT ON COLUMN company_subsidies.government_departments IS '担当府省';
COMMENT ON COLUMN company_subsidies.subsidy_resource IS '補助金財源';
COMMENT ON COLUMN company_subsidies.target           IS '対象';
COMMENT ON COLUMN company_subsidies.note             IS '備考';
COMMENT ON COLUMN company_subsidies.key_field        IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_subsidies.created_at       IS '作成日時';
COMMENT ON COLUMN company_subsidies.updated_at       IS '更新日時';

-- ── company_patents（特許情報） ──────────────────────────────────────────────
COMMENT ON TABLE  company_patents                    IS '特許情報（特許・意匠・商標）';
COMMENT ON COLUMN company_patents.id                 IS 'サロゲートキー';
COMMENT ON COLUMN company_patents.corporate_number   IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_patents.patent_type        IS '種別（特許/意匠/商標）';
COMMENT ON COLUMN company_patents.title              IS '発明の名称または意匠に係る物品または表示用商標';
COMMENT ON COLUMN company_patents.date_of_patent     IS '出願年月日';
COMMENT ON COLUMN company_patents.patent_number      IS '登録番号';
COMMENT ON COLUMN company_patents.key_field          IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_patents.created_at         IS '作成日時';
COMMENT ON COLUMN company_patents.updated_at         IS '更新日時';

-- ── company_procurements（調達情報） ─────────────────────────────────────────
COMMENT ON TABLE  company_procurements               IS '調達情報（政府調達契約）';
COMMENT ON COLUMN company_procurements.id            IS 'サロゲートキー';
COMMENT ON COLUMN company_procurements.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_procurements.title         IS '件名（事業名）';
COMMENT ON COLUMN company_procurements.amount        IS '落札価格（円）';
COMMENT ON COLUMN company_procurements.date_of_order IS '受注日';
COMMENT ON COLUMN company_procurements.government_departments IS '発注機関名';
COMMENT ON COLUMN company_procurements.key_field     IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_procurements.created_at    IS '作成日時';
COMMENT ON COLUMN company_procurements.updated_at    IS '更新日時';

-- ── company_certifications（届出・認定情報） ─────────────────────────────────
COMMENT ON TABLE  company_certifications             IS '届出・認定情報';
COMMENT ON COLUMN company_certifications.id          IS 'サロゲートキー';
COMMENT ON COLUMN company_certifications.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_certifications.title       IS '届出・認定等の名称';
COMMENT ON COLUMN company_certifications.category   IS '部門';
COMMENT ON COLUMN company_certifications.date_of_approval IS '認定日（証明日）';
COMMENT ON COLUMN company_certifications.expire_date IS '有効期限';
COMMENT ON COLUMN company_certifications.government_departments IS '認定機関（府省）';
COMMENT ON COLUMN company_certifications.key_field   IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_certifications.created_at  IS '作成日時';
COMMENT ON COLUMN company_certifications.updated_at  IS '更新日時';

-- ── company_commendations（表彰情報） ────────────────────────────────────────
COMMENT ON TABLE  company_commendations              IS '表彰情報';
COMMENT ON COLUMN company_commendations.id           IS 'サロゲートキー';
COMMENT ON COLUMN company_commendations.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_commendations.title        IS '表彰名';
COMMENT ON COLUMN company_commendations.date_of_commendation IS '表彰年月日';
COMMENT ON COLUMN company_commendations.government_departments IS '表彰機関（府省）';
COMMENT ON COLUMN company_commendations.category     IS '部門';
COMMENT ON COLUMN company_commendations.key_field    IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_commendations.created_at   IS '作成日時';
COMMENT ON COLUMN company_commendations.updated_at   IS '更新日時';

-- ── company_workplaces（職場情報） ───────────────────────────────────────────
COMMENT ON COLUMN company_workplaces.id              IS 'サロゲートキー';
COMMENT ON COLUMN company_workplaces.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_workplaces.average_age     IS '従業員の平均年齢（歳）';
COMMENT ON COLUMN company_workplaces.average_continuous_service_years IS '正社員の平均継続勤務年数（年）';
COMMENT ON COLUMN company_workplaces.average_continuous_service_years_male IS '平均継続勤務年数（男性）（年）';
COMMENT ON COLUMN company_workplaces.average_continuous_service_years_female IS '平均継続勤務年数（女性）（年）';
COMMENT ON COLUMN company_workplaces.average_continuous_service_years_type IS '平均継続勤務年数の範囲区分';
COMMENT ON COLUMN company_workplaces.month_average_overtime IS '月平均所定外労働時間（時間）';
COMMENT ON COLUMN company_workplaces.female_workers_proportion IS '労働者に占める女性労働者の割合（%）';
COMMENT ON COLUMN company_workplaces.female_workers_proportion_type IS '女性労働者割合の範囲区分';
COMMENT ON COLUMN company_workplaces.female_share_of_manager IS '女性管理職人数（人）';
COMMENT ON COLUMN company_workplaces.gender_total_of_manager IS '管理職全体人数（男女計）（人）';
COMMENT ON COLUMN company_workplaces.female_share_of_officers IS '女性役員人数（人）';
COMMENT ON COLUMN company_workplaces.gender_total_of_officers IS '役員全体人数（男女計）（人）';
COMMENT ON COLUMN company_workplaces.paternity_leave_acquisition_num IS '育児休業取得者数（男性）（人）';
COMMENT ON COLUMN company_workplaces.number_of_paternity_leave IS '育児休業対象者数（男性）（人）';
COMMENT ON COLUMN company_workplaces.maternity_leave_acquisition_num IS '育児休業取得者数（女性）（人）';
COMMENT ON COLUMN company_workplaces.number_of_maternity_leave IS '育児休業対象者数（女性）（人）';
COMMENT ON COLUMN company_workplaces.key_field       IS 'gBizINFO付番の一意識別子';
COMMENT ON COLUMN company_workplaces.created_at      IS '作成日時';
COMMENT ON COLUMN company_workplaces.updated_at      IS '更新日時';

-- ── company_web_supplements（Webスクレイピング補完情報） ─────────────────────
COMMENT ON TABLE  company_web_supplements            IS 'Webスクレイピングによる補完情報';
COMMENT ON COLUMN company_web_supplements.id         IS 'サロゲートキー';
COMMENT ON COLUMN company_web_supplements.corporate_number IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_web_supplements.source_url IS 'スクレイピング対象URL';
COMMENT ON COLUMN company_web_supplements.description IS 'スクレイピングで取得した説明文';
COMMENT ON COLUMN company_web_supplements.employees_count IS 'スクレイピングで取得した従業員数';
COMMENT ON COLUMN company_web_supplements.founded_year IS 'スクレイピングで取得した設立年';
COMMENT ON COLUMN company_web_supplements.raw_html_path IS '生HTMLの保存パス';
COMMENT ON COLUMN company_web_supplements.scraped_at IS 'スクレイピング実行日時';
COMMENT ON COLUMN company_web_supplements.created_at IS '作成日時';
COMMENT ON COLUMN company_web_supplements.updated_at IS '更新日時';

-- ── company_financial_statements（決算情報） ─────────────────────────────────
COMMENT ON COLUMN company_financial_statements.id                IS 'サロゲートキー';
COMMENT ON COLUMN company_financial_statements.corporate_number  IS '法人番号（FK: companies）';
COMMENT ON COLUMN company_financial_statements.period            IS '決算期（例: 第54期決算公告）';
COMMENT ON COLUMN company_financial_statements.release_date      IS '官報掲載日または定時株主総会日';
COMMENT ON COLUMN company_financial_statements.unit              IS '金額の単位（例: 単位：千円）';
COMMENT ON COLUMN company_financial_statements.status            IS 'ステータス（Add: 追加 / Replace: 差替）';
COMMENT ON COLUMN company_financial_statements.report_data       IS '報告書データ（JSONB）: [{reportName, asOfDate, divisions:[{name, items:[{subject,amount}]}]}]';
COMMENT ON COLUMN company_financial_statements.key_field         IS 'gBizINFO付番の一意識別子（UPSERTキー）';
COMMENT ON COLUMN company_financial_statements.created_at        IS '作成日時';
COMMENT ON COLUMN company_financial_statements.updated_at        IS '更新日時';

-- ── scraping_queue（スクレイピングキュー） ───────────────────────────────────
COMMENT ON COLUMN scraping_queue.id                  IS 'サロゲートキー';
COMMENT ON COLUMN scraping_queue.corporate_number    IS '法人番号（FK: companies）';
COMMENT ON COLUMN scraping_queue.target_url          IS 'スクレイピング対象URL';
COMMENT ON COLUMN scraping_queue.retry_count         IS '再試行回数';
COMMENT ON COLUMN scraping_queue.last_attempted_at   IS '最後にスクレイピングを試みた日時';
COMMENT ON COLUMN scraping_queue.scraped_at          IS 'スクレイピング成功日時';
COMMENT ON COLUMN scraping_queue.error_message       IS '直近のエラーメッセージ';
COMMENT ON COLUMN scraping_queue.created_at          IS '作成日時';
COMMENT ON COLUMN scraping_queue.updated_at          IS '更新日時';
