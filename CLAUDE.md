# CompanyInfoLibrary - CLAUDE.md

## サービス概要

企業情報を収集・ライブラリ化するサービス。
経済産業省が提供する **gBizINFO** をベースとした公的企業情報の収集・管理を行い、
営業支援・投資調査等での企業情報収集を目的とする。

### 主なユースケース

- 営業チームによるターゲット企業の情報収集・リスト管理
- 投資家・アナリストによる企業分析・デューデリジェンス

### 収集する企業情報

| カテゴリ | 主な項目 |
|----------|---------|
| 基本情報 | 法人番号・法人名・住所・代表者名・資本金・従業員数・業種・設立年・企業HP |
| 財務情報 | 売上高・営業収益・当期純利益・経常利益・総資産額・純資産額（年度別） |
| 補助金情報 | 補助金名・交付金額・担当府省・認定日 |
| 特許情報 | 特許取得情報 |
| 調達情報 | 政府調達契約情報 |
| 届出・認定情報 | 各種認定取得状況 |
| 表彰情報 | 受賞歴 |
| 職場情報 | 採用・職場環境情報（※gBizINFO はデータ欠如が多い） |
| 補完情報 | 企業WebサイトからのWebスクレイピング情報 |

---

## アーキテクチャ概要

### コンポーネント構成

```
[ Vue.js フロントエンド (SPA) ]
         ↓ REST API (JSON)
[ Spring Boot APIサーバー ]
    ↓ JPA/MyBatis
[ DB（PostgreSQL 候補）] ←→ [検索エンジン（Elasticsearch or pg_trgm）]
    ↑ 書込
[ Spring Boot バッチサーバー ]
    ├── Job1: gBizINFO 初回ダウンロードインポート
    ├── Job2: gBizINFO 日次差分更新（/updateInfo API）
    └── Job3: 企業Webサイト スクレイピング（並列・優先度付き）
              ↓
         [ AWS SQS（メッセージキュー）]
              ↓ ← 複数の並列ワーカーが処理
         [ 企業Webサイト（Webスクレイピング対象）]

外部データソース:
- gBizINFO REST API v2: https://api.info.gbiz.go.jp/hojin/v2/
- gBizINFO データダウンロード: 初回ロード用一括ファイル（CSV/JSON）
```

### データフロー

```
【初回ロード（1回限り）】
gBizINFO ダウンロードサイト
  → 一括ファイル（CSV/JSON）取得（500万件超）
  → Spring Batch チャンク処理（Read → Process → DB UPSERT）

【日次更新（毎日定時実行）】
gBizINFO /v2/hojin/updateInfo API（前日差分）
  → Spring Batch 差分更新ジョブ
  → 変更法人のみ各カテゴリ API を再取得 → DB UPSERT

【補完情報収集（段階的・継続実行）】
DB内の企業HP URL（優先度付きで選択）
  → SQS キューへ投入
  → 並列スクレイピングワーカー（robots.txt 遵守・レート制限あり）
  → DB（補完情報テーブルへ UPSERT）
```

---

## 技術スタック

| コンポーネント | 技術 |
|--------------|------|
| APIサーバー | Java / Spring Boot |
| バッチサーバー | Java / Spring Boot + Spring Batch |
| フロントエンド | Vue.js |
| インフラ | Docker / Docker Compose / AWS（ECS + SQS 等）予定 |
| DB | 未定（PostgreSQL 推奨。詳細は「DB設計方針」参照）|
| メッセージキュー | AWS SQS（スクレイピングJob用）|

---

## gBizINFO 連携仕様

### データ取得戦略

| フェーズ | 方法 | 理由 |
|---------|------|------|
| 初回ロード | **データダウンロード**（CSV/JSON 一括） | 500万件超のため API 逐次呼び出し禁止 |
| 日次更新 | **`/updateInfo` API**（前日差分） | 変更分のみ効率的に取得 |
| 補完情報 | **Webスクレイピング**（優先度付き並列処理） | 企業HP 等から gBizINFO にない情報を補完 |

### REST API v2 エンドポイント一覧

Base URL: `https://api.info.gbiz.go.jp/hojin/v2`

```
GET /hojin                                   # 法人検索
GET /hojin/{corporate_number}                # 法人基本情報取得
GET /hojin/{corporate_number}/certification  # 届出・認定情報
GET /hojin/{corporate_number}/finance        # 財務情報
GET /hojin/{corporate_number}/patent         # 特許情報
GET /hojin/{corporate_number}/procurement    # 調達情報
GET /hojin/{corporate_number}/subsidy        # 補助金情報
GET /hojin/{corporate_number}/commendation   # 表彰情報
GET /hojin/{corporate_number}/workplace      # 職場情報
GET /hojin/updateInfo                        # 期間指定での更新情報（差分取得）
GET /hojin/updateInfo/{category}             # カテゴリ別差分取得
```

- **API 利用申請**: APIトークンの申請が必要
- **Swagger UI**: `https://api.info.gbiz.go.jp/hojin/swagger-ui/index.html?urls.primaryName=v2`

### v1 → v2 の主な変更点

| 観点 | v1 | v2 |
|------|----|----|
| Base URL | `info.gbiz.go.jp` | `api.info.gbiz.go.jp` |
| 取得データ | 基本情報・財務・補助金・特許・調達・表彰・職場 | 上記に加え ESG 関連データ・メタデータを拡充 |
| 検索機能 | 基本検索 | パラメータ強化（詳細は Swagger v2 参照） |
| セキュリティ | APIトークン | 認証品質向上（詳細は Swagger v2 参照）|
| 廃止予定 | 2026年9月 | — |

> フィールドレベルの詳細差分は実装時に公式 Swagger UI（v2）で確認すること。

### 主要データフィールド

**法人基本情報 (HojinInfo)**
```
corporate_number  法人番号（13桁・全テーブルの主キー）
name              法人名
kana              法人名フリガナ
name_en           法人名英語
location          本社所在地
postal_code       郵便番号
company_url       企業ホームページ URL
business_summary  事業概要
capital_stock     資本金
employee_number   従業員数（全体）
company_size_male / company_size_female  男女別規模
representative_name     代表者名
representative_position 代表者役職
date_of_establishment   設立年月日
founding_year           創業年
update_date             最終更新日
business_items          営業品目リスト
status                  法人ステータス
```

**財務情報 (ManagementIndex)**
```
net_sales                      売上高
gross_operating_revenue        営業総収入
operating_revenue1/2           営業収益/収入
net_income_loss                当期純利益/損失
ordinary_income_loss           経常利益/損失
total_assets                   総資産額
net_assets                     純資産額
capital_stock                  資本金
number_of_employees            従業員数
period                         回次（年度）
accounting_standards           会計基準
major_shareholders             大株主情報
```

**補助金情報 (SubsidyInfo)**
```
title                補助金名称
amount               交付金額
date_of_approval     認定日
government_departments 担当府省
subsidy_resource     補助金財源
target               対象
```

---

## Webスクレイピング方針

### 実施上の制約（必ず遵守）

- `robots.txt` を取得・確認し、禁止パスへのアクセスは行わない
- 同一ドメインへのアクセス間隔: **最低1〜3秒**（ドメインレベルのレート制限を実装）
- `User-Agent` にサービス名・連絡先を明記する
- 著作権・利用規約を確認してからスクレイピングする

### 対象フェーズ（段階的実施）

1. **Phase 1**: 大企業・上場企業（資本金・従業員数の閾値でフィルタ）
2. **Phase 2**: 中堅企業
3. **Phase 3**: 必要に応じて小規模企業

---

## バッチジョブ設計

### Job1: gBizINFO 初回ダウンロードインポート（1回限り）

```
Spring Batch 構成:
- ItemReader  : CSV/JSON ファイル読み込み（FlatFileItemReader or JsonItemReader）
- ItemProcessor: データ変換・バリデーション
- ItemWriter  : DB UPSERT（JPA or JdbcBatchItemWriter）
- チャンクサイズ: 1,000〜5,000件（パフォーマンスに応じてチューニング）
```

### Job2: gBizINFO 日次差分更新（毎日定時実行）

```
Spring Batch 構成:
- Step1: /updateInfo API で更新法人番号リストを取得
- Step2: 各法人番号に対して各カテゴリ API を呼び出し → DB UPSERT
- 処理量: 通常 数千〜数万件/日 程度
- スケジュール: Spring Scheduler or AWS EventBridge
```

### Job3: 企業Webサイト スクレイピング（並列分散処理）

500万件超の企業HP を1ジョブで処理すると完了しないため、並列分散処理を採用する。

```
Spring Batch 構成:
- Step1: スクレイピング対象 URL をキューイング
    DB から企業HP URL を優先度順（資本金・従業員数等）に取得
    → AWS SQS へ投入

- Step2: パーティション分割 × 並列ワーカー
    Spring Batch RemotePartitioning or MultiThreadedStep
    → 各ワーカーが SQS からURLを取り出してスクレイピング
    → HTML 解析（Jsoup 等）→ DB UPSERT

管理情報（DB）:
- 最終スクレイピング日時
- 取得ステータス（成功/失敗/スキップ）
- 再試行回数
※ 冪等性を保ち再実行可能にすること
```

---

## DB設計方針

- **対象レコード数**: 500万件超
- **主キー**: 法人番号（13桁） — 全テーブルで一元管理
- **テーブル設計（例）**:
  - `companies` 法人基本情報
  - `company_finances` 財務情報（年度別）
  - `company_subsidies` 補助金情報
  - `company_patents` 特許情報
  - `company_procurements` 調達情報
  - `company_certifications` 届出・認定情報
  - `company_commendations` 表彰情報
  - `company_workplaces` 職場情報
  - `company_web_supplements` Webスクレイピング補完情報
  - `scraping_queue` スクレイピングキュー管理

- **検索性確保の選択肢**:
  - **Option A**: PostgreSQL + pg_trgm（あいまい検索） — シンプル構成、初期推奨
  - **Option B**: PostgreSQL + Elasticsearch — 高度な全文検索・ファセット検索が必要な場合

---

## ディレクトリ構造（モノレポ暫定案）

```
/
├── backend/          # Spring Boot APIサーバー
│   └── src/
│       └── main/java/
│           ├── controller/   # REST エンドポイント
│           ├── service/      # ビジネスロジック
│           ├── repository/   # DB アクセス
│           └── domain/       # エンティティ・DTO
├── batch/            # Spring Boot バッチサーバー
│   └── src/
│       └── main/java/
│           ├── job/          # Batch ジョブ定義
│           ├── step/         # Batch ステップ定義
│           ├── reader/       # ItemReader
│           ├── processor/    # ItemProcessor
│           ├── writer/       # ItemWriter
│           └── infrastructure/  # 外部API・スクレイピング実装
├── frontend/         # Vue.js フロントエンド
├── docker/           # Dockerfile・Docker Compose 設定
├── docs/             # 設計ドキュメント・API仕様
└── CLAUDE.md
```

---

## 開発ガイドライン

### バックエンド（Spring Boot）

- **レイヤードアーキテクチャ**を採用: `Controller → Service → Repository`
- Controller は HTTP の責務のみ（バリデーション・レスポンス変換）
- ビジネスロジックは Service に集約
- 外部API・スクレイピングの実装は `infrastructure` レイヤーに分離
- 環境変数で設定を外部化（ハードコード禁止）

### バッチ（Spring Batch）

- ジョブ・ステップ・チャンク指向処理で設計
- 各ステップを明確に分割し、再起動・スキップ処理を考慮
- **冪等性を必ず確保**（重複実行・再実行しても結果が同じになること）
- DB の UPSERT（INSERT ON CONFLICT UPDATE）を基本とする

### フロントエンド（Vue.js）

- コンポーネント単位で責務を分離
- API 呼び出しは composables または Pinia store に集約

### インフラ・コンテナ

- 各サービス（API・バッチ・DB）は独立した Docker イメージとして構築
- ローカル開発は Docker Compose で完結させる
- AWS 展開を前提とした設計（ECS, RDS, SQS, S3 等の利用を想定）

---

## 開発コマンド

> 環境構築後に追記予定
