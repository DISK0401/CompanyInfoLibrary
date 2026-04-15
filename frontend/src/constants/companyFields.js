export const FIELD_TYPE = {
  TEXT: 'text',
  NUMBER: 'number',
  DATE: 'date',
}

// companyテーブルの全フィールド（検索・一覧表示に使用）
export const COMPANY_FIELDS = [
  { key: 'corporateNumber',    label: '法人番号',         type: FIELD_TYPE.TEXT,   minWidth: 140 },
  { key: 'name',               label: '法人名',           type: FIELD_TYPE.TEXT,   minWidth: 200 },
  { key: 'kana',               label: 'フリガナ',          type: FIELD_TYPE.TEXT,   minWidth: 180 },
  { key: 'nameEn',             label: '法人名（英語）',    type: FIELD_TYPE.TEXT,   minWidth: 200 },
  { key: 'location',           label: '所在地',            type: FIELD_TYPE.TEXT,   minWidth: 200 },
  { key: 'postalCode',         label: '郵便番号',          type: FIELD_TYPE.TEXT,   minWidth: 100 },
  { key: 'companyUrl',         label: '企業HP',            type: FIELD_TYPE.TEXT,   minWidth: 200 },
  { key: 'businessSummary',    label: '事業概要',          type: FIELD_TYPE.TEXT,   minWidth: 250 },
  { key: 'status',             label: 'ステータス',        type: FIELD_TYPE.TEXT,   minWidth: 100 },
  { key: 'kind',               label: '法人種別',          type: FIELD_TYPE.TEXT,   minWidth: 100 },
  { key: 'closeCause',         label: '閉鎖事由',          type: FIELD_TYPE.TEXT,   minWidth: 150 },
  { key: 'qualificationGrade', label: '格付け',            type: FIELD_TYPE.TEXT,   minWidth: 150 },
  { key: 'representativeName', label: '代表者名',          type: FIELD_TYPE.TEXT,   minWidth: 150 },
  { key: 'capitalStock',       label: '資本金',            type: FIELD_TYPE.NUMBER, minWidth: 130 },
  { key: 'employeeNumber',     label: '従業員数',          type: FIELD_TYPE.NUMBER, minWidth: 100 },
  { key: 'companySizeMale',    label: '男性従業員数',      type: FIELD_TYPE.NUMBER, minWidth: 110 },
  { key: 'companySizeFemale',  label: '女性従業員数',      type: FIELD_TYPE.NUMBER, minWidth: 110 },
  { key: 'foundingYear',       label: '創業年',            type: FIELD_TYPE.NUMBER, minWidth: 90  },
  { key: 'dateOfEstablishment',label: '設立年月日',        type: FIELD_TYPE.DATE,   minWidth: 110 },
  { key: 'closeDate',          label: '閉鎖日',            type: FIELD_TYPE.DATE,   minWidth: 100 },
  { key: 'gbizinfoUpdateDate', label: 'gBizINFO更新日',   type: FIELD_TYPE.DATE,   minWidth: 120 },
]

// matchTypeのラベルと対象型
export const TEXT_MATCH_TYPES = [
  { value: 'CONTAINS',    label: '部分一致' },
  { value: 'EXACT',       label: '完全一致' },
  { value: 'PREFIX',      label: '前方一致' },
  { value: 'SUFFIX',      label: '後方一致' },
  { value: 'FUZZY',       label: 'あいまい一致' },
]

export const NUMBER_DATE_MATCH_TYPES = [
  { value: 'EQ',    label: '= 等しい' },
  { value: 'NEQ',   label: '≠ 等しくない' },
  { value: 'GTE',   label: '≥ 以上' },
  { value: 'LTE',   label: '≤ 以下' },
  { value: 'RANGE', label: '範囲（from〜to）' },
]

// デフォルト表示列
export const DEFAULT_VISIBLE_COLUMNS = [
  'corporateNumber', 'name', 'location', 'capitalStock', 'employeeNumber', 'representativeName', 'foundingYear'
]
