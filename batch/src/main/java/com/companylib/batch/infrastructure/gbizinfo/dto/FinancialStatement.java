package com.companylib.batch.infrastructure.gbizinfo.dto;

import lombok.Data;
import java.util.List;

/**
 * Kessanjoho XML ファイル1件から生成する決算情報 DTO。
 * 1 XML ファイル = 1 FinancialStatement（複数の Report を含む場合あり）。
 */
@Data
public class FinancialStatement {

    private String corporateNumber;
    private String period;        // "第XX期決算公告"
    private String releaseDate;   // "YYYY年M月D日"
    private String companyName;
    private String unit;          // "単位：千円"
    private String status;        // "Add" or "Replace"
    private String keyField;

    /** 報告書リスト（貸借対照表の要旨 / 損益計算書の要旨 等） */
    private List<Report> reports;

    @Data
    public static class Report {
        private String reportName;  // 貸借対照表の要旨 等
        private String asOfDate;    // 日付または期間

        private List<Division> divisions;
    }

    @Data
    public static class Division {
        private String name;        // 資産の部 / 負債及び純資産の部 等

        private List<LineItem> items;
    }

    @Data
    public static class LineItem {
        private String subject; // 勘定科目名
        private String amount;  // 金額（文字列のまま保持）
    }
}
