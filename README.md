# CompanyInfoLibrary

企業情報を収集しライブラリ化するサービス。
gBizINFO（経済産業省）をベースとした公的企業情報の収集・管理と、
Webスクレイピングによる補完情報の収集を行う。

## 構成

| コンポーネント | 技術 | ポート |
|---|---|---|
| APIサーバー | Java 17 / Spring Boot 3.3 | 8080 |
| バッチサーバー | Java 17 / Spring Boot 3.3 + Spring Batch | — |
| フロントエンド | Vue.js 3 / Vite | 3000 |
| DB | PostgreSQL 16 | 5432 |

---

## 必要な環境

- **Java 17+**（`java -version` で確認）
- **Maven 3.9+**（`mvn -version` で確認）
- **Node.js 20+** / **npm 10+**（`node -v` / `npm -v` で確認）
- **Docker** / **Docker Compose v2**（`docker compose version` で確認）

---

## クイックスタート（Docker Compose）

DB・バックエンド・バッチ・フロントエンドをまとめて起動する方法。

### 1. 環境変数の設定

```bash
cp .env.example .env
# .env を開き GBIZINFO_API_TOKEN に取得済みのトークンを設定
```

### 2. 起動

```bash
cd docker
docker compose up -d
```

| サービス | URL |
|---|---|
| フロントエンド | http://localhost:3000 |
| API | http://localhost:8080 |
| DB | localhost:5432 |

### 3. 停止・クリーンアップ

```bash
# 停止（データ保持）
docker compose down

# 停止 + DB データ削除
docker compose down -v
```

---

## ローカル開発（個別起動）

各コンポーネントを個別に起動して開発する方法。

### DB のみ Docker で起動

```bash
cd docker
docker compose up -d db
```

### バックエンド（APIサーバー）

```bash
cd backend
mvn spring-boot:run
# または
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- 起動確認: http://localhost:8080/api/companies

### バッチサーバー

```bash
cd batch
mvn spring-boot:run
```

バッチジョブは起動時に自動実行されません（`spring.batch.job.enabled=false`）。
ジョブの実行は後述の「バッチジョブの実行」を参照してください。

### フロントエンド

```bash
cd frontend
npm install
npm run dev
```

- 起動確認: http://localhost:3000
- `/api` へのリクエストは自動的に `http://localhost:8080` へプロキシされます

---

## ビルド

### バックエンド / バッチ（JAR）

```bash
# プロジェクトルートから全モジュールをビルド
mvn package -DskipTests

# backend のみ
mvn -pl backend -am package -DskipTests

# batch のみ
mvn -pl batch -am package -DskipTests
```

### フロントエンド（本番ビルド）

```bash
cd frontend
npm run build
# dist/ に出力される
```

---

## テスト

### バックエンド / バッチ

```bash
# 全テスト実行
mvn test

# backend のみ
mvn -pl backend test

# batch のみ
mvn -pl batch test
```

### フロントエンド

```bash
cd frontend
npm run lint
```

---

## バッチジョブの実行

バッチサーバー起動後、Spring Boot Actuator または直接 `JobLauncher` を呼び出します。
開発中は以下のように起動時にジョブ名をパラメータで渡すことで実行できます。

```bash
# Job1: gBizINFO 初回インポート（JSON ファイルを指定）
cd batch
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=gbizInfoImportJob inputFilePath=/path/to/hojin.json"

# Job2: gBizINFO 日次差分更新（前日分）
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=gbizInfoDailyUpdateJob"

# Job2: 期間指定で実行
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31"
```

---

## DB マイグレーション（Flyway）

スキーマは Flyway で自動管理されます。アプリ起動時に自動適用されます。

```bash
# マイグレーション状態確認（backend から実行）
cd backend
mvn flyway:info

# 手動マイグレーション実行
mvn flyway:migrate
```

マイグレーションファイルの場所:
- `backend/src/main/resources/db/migration/` — 共通スキーマ（V0〜V11）
- `batch/src/main/resources/db/migration/` — バッチ専用テーブル（V100〜）

---

## 環境変数一覧

| 変数名 | デフォルト値 | 説明 |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/companylib` | DB 接続 URL |
| `DB_USERNAME` | `companylib` | DB ユーザー名 |
| `DB_PASSWORD` | `companylib` | DB パスワード |
| `GBIZINFO_API_TOKEN` | （空） | gBizINFO API トークン（要申請） |
| `GBIZINFO_API_BASE_URL` | `https://api.info.gbiz.go.jp/hojin/v2` | gBizINFO API ベース URL |
| `JPA_DDL_AUTO` | `validate` | Hibernate DDL モード |
| `JPA_SHOW_SQL` | `false` | SQL ログ出力 |
| `FLYWAY_BASELINE_ON_MIGRATE` | `false` | 既存 DB への初回マイグレーション |
| `SCRAPING_INTERVAL_MS` | `2000` | スクレイピング間隔（ms） |
| `LOG_LEVEL` | `INFO` | アプリログレベル |
| `BATCH_LOG_LEVEL` | `INFO` | Spring Batch ログレベル |
| `SERVER_PORT` | `8080` | API サーバーポート |

---

## ディレクトリ構造

```
/
├── backend/          # Spring Boot APIサーバー
│   └── src/main/
│       ├── java/com/companylib/api/
│       │   ├── controller/   # REST エンドポイント
│       │   ├── domain/       # エンティティ・DTO
│       │   ├── repository/   # DB アクセス
│       │   └── service/      # ビジネスロジック
│       └── resources/
│           ├── application.yml
│           └── db/migration/ # Flyway マイグレーション（共通）
├── batch/            # Spring Boot バッチサーバー
│   └── src/main/
│       ├── java/com/companylib/batch/
│       │   ├── infrastructure/  # gBizINFO API クライアント・スクレイピング
│       │   └── job/             # Spring Batch ジョブ定義
│       └── resources/
│           ├── application.yml
│           └── db/migration/ # Flyway マイグレーション（バッチ専用）
├── frontend/         # Vue.js 3 フロントエンド
├── docker/           # Dockerfile・Docker Compose
├── docs/             # 設計ドキュメント
├── pom.xml           # Maven 親 POM
├── .env.example      # 環境変数テンプレート
└── CLAUDE.md         # AI 開発支援用コンテキスト
```
