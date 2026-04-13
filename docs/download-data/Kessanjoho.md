# 決算情報XMLファイル

他とはことなり、個々の決算情報ごとにファイルが存在する。

※「Metadata」タグ内の項目は、メタデータである。

| 通番 | 深さ | 要素情報 | データ型 | 説明 |
| --- | --- | --- | --- | --- |
| 要素名 | タグ名 |
| 1 | 01 | 決算情報 | FinancialInformation | - | - |
| 2 | 02 | 官報掲載情報 | KanpouPostedInformation | - | - |
| 3 | 03 | 記番号 | DataName | string | データ識別番号 |
| 4 | 03 | ステータス | Status | string | 公開ステータス・Add：追加（データを追加）・Replace：差替（官報の訂正・正誤記事に基づく修正） |
| 5 | 03 | 発行日 | IssueDate | string | 官報発行日YYYY年MM月DD日で表記 |
| 6 | 03 | 官報種 | Classification | string | 官報の発行形態（本紙、号外などの別） |
| 7 | 03 | 号数 | Number | string | 官報掲載号数 |
| 8 | 03 | 頁 | Page | string | 官報掲載頁 |
| 9 | 02 | 法人情報 | CorporateInformation | - |  |
| 10 | 03 | 期 | Period | string | 決算公告期 |
| 11 | 03 | 公開日(官報掲載日又は定時株主総会日) | Release | string | 公開日(官報掲載日又は定時株主総会日)YYYY年M月D日で表記 |
| 12 | 03 | 法人名 | CompanyName | string | 法人名 |
| 13 | 03 | 単位 | Unit | string | 金額の単位 |
| 14 | 03 | 法人番号 | CorporateNumber | string | 法人番号 |
| 15 | 02 | 表の情報 | Report | - |  |
| 16 | 03 | 表名 | ReportName | string | 表名・貸借対照表の要旨・損益計算書の要旨 |
| 17 | 04 | 日付または期間 | BsPlDate | string | 日付または期間・YYYY年M月D日現在・自YYYY年M月D日～至YYYY年M月D日 |
| 18 | 05 | 部 | Division | string | 部・資産の部・負債及び純資産の部・負債及び正味財産の部・負債の部・純資産の部 |
| 19 | 06 | 明細情報 | Meisai | - |  |
| 20 | 07 | 勘定科目名 | Subject | string | 勘定科目名 |
| 21 | 07 | 金額 | Amount | string | 金額 |
| 22 | 02 | メタデータ | Metadata | - |  |
| 23 | 03 | キー情報 | KeyField | string | gBizINFOが付番する一意となる識別子を示す。 |
| 24 | 03 | データ品質 | DataQuality | string | YYYY年MM月DD日 |
| 25 | 03 | 出典元 | Source | string | データを取得したシステム名称または行政庁名を示す。 |
| 26 | 03 | データ取込頻度 | ImportFrequency | string | Gビズインフォへのデータ取込頻度を示す。固定値「月次」 |
| 27 | 03 | 最終取得日 | Issued | string | データを外部システムから最後に取得した日付を示す。入力形式は以下のとおりとする。・YYYY年MM月DD日 |
| 28 | 03 | 最終更新日 | Updated | string | Gビズインフォ上でデータが最後に更新・修正された日付を示す。入力形式は以下のとおり。・YYYY年MM月DD日 |

ファイル例はこちら

```xml
<?xml version="1.0" encoding="UTF-8"?>
<FinancialInformation>
<KanpouPostedInformation>
 <DataName>hoge</DataName>
 <Status>Add</Status>
 <IssueDate>YYYY年MM月DD日</IssueDate>
 <Classification>号外</Classification>
 <Number>XX</Number>
 <Page>XX</Page>
</KanpouPostedInformation>
<CorporateInformation>
 <Period>第XX期決算公告</Period>
 <Release>YYYY年M月D日</Release>
 <CompanyName>Meti株式会社</CompanyName>
 <Unit>単位：千円</Unit>
 <CorporateNumber>1234567890123</CorporateNumber>
</CorporateInformation>
<Report>
 <ReportName 表名="貸借対照表の要旨" > 
 <BsPlDate 日付="YYYY年MM月DD日現在" > 
 <Division 部="資産の部" > 
 <Meisai>
  <Subject>流動資産</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>固定資産</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　　　　合計</Subject>
  <Amount>99999</Amount>
 </Meisai>
</Division>
 <Division 部="負債及び純資産の部" > 
 <Meisai>
  <Subject>流動負債</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　（賞与引当金）</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>固定負債</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　（退職給付引当金）</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>株主資本</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　資本金</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　利益剰余金</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　　利益準備金</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　　その他利益剰余金</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　　（うち当期純利益）</Subject>
  <Amount>99999</Amount>
 </Meisai>
 <Meisai>
  <Subject>　　　　合計</Subject>
  <Amount>99999</Amount>
 </Meisai>
</Division>
</BsPlDate>
</ReportName>
</Report>
 <Metadata>
  <KeyField>2025_08_95_20250101_00000001</KeyField>
  <DataQuality>政府連携データ</DataQuality>
  <Source>国立印刷局</Source>
  <ImportFrequency>月次</ImportFrequency>
  <Issued>2025年01月01日</Issued>
  <Updated>2025年01月01日</Updated>
 </Metadata>
</FinancialInformation>
```

