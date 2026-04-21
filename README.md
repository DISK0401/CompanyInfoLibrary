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

### 4. サンプルデータの投入（任意）

スタック起動後に開発用サンプルデータ（5社）を投入できます。

```bash
docker exec -i companylib-db psql -U companylib -d companylib < docker/sample-data.sql
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

**bash / zsh:**

```bash
cd batch

# Job1: gBizINFO 初回インポート（JSON ファイルを指定）
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoInitialLoadJob inputFilePath=/path/to/hojin.json"

# Job2: gBizINFO 日次差分更新（前日分）
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob"

# Job2: 期間指定で実行
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31"
```

**PowerShell（Windows）— `-D` 引数はシングルクォートで囲む:**

```powershell
cd batch

# Job1: gBizINFO 初回インポート（JSON ファイルを指定）
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoInitialLoadJob inputFilePath=/path/to/hojin.json'

# Job2: gBizINFO 日次差分更新（前日分）
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob'

# Job2: 期間指定で実行
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31'

# Job2: 完了済みジョブを強制的に再実行（force=true）
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31 --force=true'
```

### 途中停止したジョブの再開

Spring Batch はジョブの実行状態を DB に保存するため、**同じパラメータで再実行すると中断点から自動的に再開**されます。

- **Step1 で失敗した場合**: Step1 から再実行（法人情報 JSON の取得をやり直し）
- **Step2 で失敗した場合**: Step1（完了済み）はスキップし、未処理分の続きから再開

再開するには、**失敗時と同じパラメータ**（`from` / `to`）で再度実行するだけです。

```powershell
# 失敗したときと同じコマンドを再実行する
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31'
```

> **注意**: `from` / `to` を省略した場合（前日デフォルト）は、再実行する日が変わると異なるパラメータとして扱われ新規実行になります。再開したい場合は必ず同じ日付を明示してください。

### 完了済みジョブの強制再実行

既に COMPLETED のジョブを同じパラメータで再実行したい場合は `--force=true` を付けてください。
メタデータを自動でクリアしてから再実行します。

```powershell
mvn spring-boot:run '-Dspring-boot.run.arguments=--spring.batch.job.enabled=true --spring.batch.job.name=gbizInfoDailyUpdateJob from=2024-01-01 to=2024-01-31 --force=true'
```

---

## DB マイグレーション（Flyway）

スキーマは Flyway で自動管理されます。アプリ起動時に自動適用されます。

> **前提**: `mvn flyway:info` / `mvn flyway:migrate` の実行には DB が起動している必要があります。
> また、**Java 17** で Maven を実行してください（`mvn -version` で確認）。

### 実行順序

**backend → batch の順で実行してください。**

backend と batch はそれぞれ独立した Flyway 履歴テーブルを持ちます。
batch の `baselineOnMigrate=true` は「スキーマが空でない状態での初回起動」を想定した設定のため、
backend が先に共通テーブルを作成している必要があります。
batch を先に実行すると、続く backend の初回マイグレーションが失敗します。

| モジュール | 履歴テーブル | 管理対象 |
|---|---|---|
| backend | `flyway_schema_history_backend` | 共通スキーマ（V0〜） |
| batch | `flyway_schema_history_batch` | バッチ専用テーブル（V100〜） |

### 手動マイグレーション実行

```bash
# 1. DB を起動（未起動の場合）
cd docker
docker compose up -d db

# 2. backend を先に実行
cd ../backend
mvn flyway:migrate

# 3. batch を実行
cd ../batch
mvn flyway:migrate
```

状態確認:

```bash
# backend
cd backend && mvn flyway:info

# batch
cd batch && mvn flyway:info
```

デフォルトの接続先は `localhost:5432/companylib`（ユーザー: `companylib`）です。
別の接続先を使う場合は `-D` フラグで上書きできます。

```bash
mvn flyway:migrate -Ddb.url=jdbc:postgresql://host:5432/companylib \
                   -Ddb.username=user \
                   -Ddb.password=pass
```

マイグレーションファイルの場所:
- `backend/src/main/resources/db/migration/` — 共通スキーマ（V0〜）
- `batch/src/main/resources/db/migration/` — バッチ専用テーブル（V0〜）

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
