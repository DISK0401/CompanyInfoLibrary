package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.KihonjohoRow;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kihonjoho CSV のフィールドを {@link KihonjohoRow} にマッピングする。
 *
 * <p>列インデックスは 0 始まりで、Kihonjoho.md の通番から 1 引いた値。
 * CSVは UTF-8・ダブルクォート囲みに対応。
 */
public class KihonjohoFieldSetMapper implements FieldSetMapper<KihonjohoRow> {

    // ── 列インデックス定数 ────────────────────────────────────────────────────
    private static final int IDX_CORPORATE_NUMBER         = 0;
    private static final int IDX_NAME                     = 1;
    private static final int IDX_KANA                     = 2;
    private static final int IDX_NAME_EN                  = 3;
    private static final int IDX_CLOSE_DATE               = 4;
    private static final int IDX_CLOSE_CAUSE              = 5;
    private static final int IDX_LOCATION                 = 6;
    private static final int IDX_POSTAL_CODE              = 7;
    // 8-12: 都道府県・市区町村等（スキップ）
    private static final int IDX_KIND                     = 13;
    private static final int IDX_PROCESS                  = 14;
    // 15: 訂正区分（スキップ）
    private static final int IDX_STATUS                   = 16;
    private static final int IDX_REPRESENTATIVE_NAME      = 17;
    private static final int IDX_CAPITAL_STOCK            = 18;
    private static final int IDX_EMPLOYEE_NUMBER          = 19;
    private static final int IDX_COMPANY_SIZE_MALE        = 20;
    private static final int IDX_COMPANY_SIZE_FEMALE      = 21;
    private static final int IDX_BUSINESS_SUMMARY         = 22;
    private static final int IDX_COMPANY_URL              = 23;
    private static final int IDX_FOUNDING_YEAR            = 24;
    private static final int IDX_BUSINESS_ITEMS           = 25;
    private static final int IDX_DATE_OF_ESTABLISHMENT    = 26;
    private static final int IDX_QUALIFICATION_GRADE      = 27;
    // 28: 全省庁統一資格-営業品目（スキップ）
    private static final int IDX_UPDATE_DATE              = 29;

    @Override
    public KihonjohoRow mapFieldSet(FieldSet fieldSet) {
        KihonjohoRow row = new KihonjohoRow();

        row.setCorporateNumber(trim(fieldSet, IDX_CORPORATE_NUMBER));
        row.setName(trim(fieldSet, IDX_NAME));
        row.setKana(trim(fieldSet, IDX_KANA));
        row.setNameEn(trim(fieldSet, IDX_NAME_EN));
        row.setCloseDateStr(trim(fieldSet, IDX_CLOSE_DATE));
        row.setCloseCause(trim(fieldSet, IDX_CLOSE_CAUSE));
        row.setLocation(trim(fieldSet, IDX_LOCATION));
        row.setPostalCode(trim(fieldSet, IDX_POSTAL_CODE));
        row.setKind(trim(fieldSet, IDX_KIND));
        row.setProcess(trim(fieldSet, IDX_PROCESS));
        row.setStatus(trim(fieldSet, IDX_STATUS));
        row.setRepresentativeName(trim(fieldSet, IDX_REPRESENTATIVE_NAME));
        row.setCapitalStockStr(trim(fieldSet, IDX_CAPITAL_STOCK));
        row.setEmployeeNumberStr(trim(fieldSet, IDX_EMPLOYEE_NUMBER));
        row.setCompanySizeMaleStr(trim(fieldSet, IDX_COMPANY_SIZE_MALE));
        row.setCompanySizeFemaleStr(trim(fieldSet, IDX_COMPANY_SIZE_FEMALE));
        row.setBusinessSummary(trim(fieldSet, IDX_BUSINESS_SUMMARY));
        row.setCompanyUrl(trim(fieldSet, IDX_COMPANY_URL));
        row.setFoundingYearStr(trim(fieldSet, IDX_FOUNDING_YEAR));
        row.setDateOfEstablishmentStr(trim(fieldSet, IDX_DATE_OF_ESTABLISHMENT));
        row.setQualificationGrade(trim(fieldSet, IDX_QUALIFICATION_GRADE));
        row.setUpdateDateStr(trim(fieldSet, IDX_UPDATE_DATE));

        // 事業種目: 「|」区切りで分割し、空文字を除外
        String businessItemsRaw = trim(fieldSet, IDX_BUSINESS_ITEMS);
        if (businessItemsRaw != null && !businessItemsRaw.isBlank()) {
            List<String> items = Arrays.stream(businessItemsRaw.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
            row.setBusinessItems(items.isEmpty() ? null : items);
        }

        return row;
    }

    /** null 安全なトリム処理。フィールドが存在しない場合は null を返す */
    private String trim(FieldSet fs, int index) {
        if (index >= fs.getFieldCount()) return null;
        String val = fs.readString(index);
        return (val == null || val.isBlank()) ? null : val.trim();
    }
}
