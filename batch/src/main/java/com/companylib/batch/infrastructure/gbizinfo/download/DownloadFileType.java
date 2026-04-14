package com.companylib.batch.infrastructure.gbizinfo.download;

/**
 * gBizINFO ダウンロードファイル種別。
 * 初回データ投入では Hojinjoho（全データ一括）と Kessanjoho（決算情報）のみを対象とする。
 */
public enum DownloadFileType {

    /** 法人情報（一括）— 基本情報・財務・補助金・特許・調達・認定・表彰・職場をすべて含む JSON */
    HOJINJOHO("Hojinjoho"),

    /** 法人基本情報 CSV — 全法人（577万件超）の基本情報 */
    KIHONJOHO("Kihonjoho"),

    /** 決算情報 — 官報掲載の貸借対照表・損益計算書 XML */
    KESSANJOHO("Kessanjoho");

    private final String downFileName;

    DownloadFileType(String downFileName) {
        this.downFileName = downFileName;
    }

    /** ダウンロード API の downfile パラメータ値 */
    public String getDownFileName() {
        return downFileName;
    }
}
