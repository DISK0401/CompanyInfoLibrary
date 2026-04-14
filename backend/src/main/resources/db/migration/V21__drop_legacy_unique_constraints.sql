-- key_field（gBizINFO 付番の一意識別子）を UPSERT キーとして採用したため、
-- 旧来の複合ユニーク制約を削除する。
-- 同一法人の同一特許番号・補助金に複数の key_field が存在するケースに対応する。

-- 特許情報: (corporate_number, patent_number) の複合制約を削除
ALTER TABLE company_patents
    DROP CONSTRAINT IF EXISTS uq_company_patents_corp_patent_number;

-- 補助金情報: (corporate_number, title, date_of_approval) の複合制約を削除
ALTER TABLE company_subsidies
    DROP CONSTRAINT IF EXISTS uq_company_subsidies_corp_title_date;
