-- representative_position カラムを削除
-- gBizINFO JSON および API v2 のいずれにも存在しないフィールドのため

ALTER TABLE companies DROP COLUMN IF EXISTS representative_position;
