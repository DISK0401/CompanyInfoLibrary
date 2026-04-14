package com.companylib.batch.infrastructure.gbizinfo.dto;

import lombok.Data;
import java.util.List;

/**
 * Kihonjoho CSV（法人基本情報）1行分の DTO。
 * 列インデックスは Kihonjoho.md の通番（1始まり）から1引いた値（0始まり）を使用する。
 *
 * <pre>
 * 主要カラムインデックス（0始まり）:
 *   0:法人番号  1:商号名称  2:カナ  3:英字  4:閉鎖年月日  5:閉鎖事由
 *   6:登記住所  7:郵便番号  13:組織種別  14:処理区分  16:状態
 *   17:代表者名称  18:資本金  19:従業員数  20:男性  21:女性
 *   22:事業概要  23:URL  24:創業年  25:事業種目(|区切り)  26:設立年月日
 *   27:全省庁統一資格等級  29:更新年月日
 * </pre>
 */
@Data
public class KihonjohoRow {

    private String corporateNumber;
    private String name;
    private String kana;
    private String nameEn;
    private String closeDateStr;
    private String closeCause;
    private String location;
    private String postalCode;
    private String kind;
    private String process;
    private String status;
    private String representativeName;
    private String capitalStockStr;
    private String employeeNumberStr;
    private String companySizeMaleStr;
    private String companySizeFemaleStr;
    private String businessSummary;
    private String companyUrl;
    private String foundingYearStr;
    /** 事業種目（「|」区切りのコードリスト） */
    private List<String> businessItems;
    private String dateOfEstablishmentStr;
    private String qualificationGrade;
    private String updateDateStr;
}
