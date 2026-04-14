package com.companylib.batch.infrastructure.gbizinfo;

import com.companylib.batch.infrastructure.gbizinfo.dto.FinancialStatement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Kessanjoho ディレクトリ内の XML ファイルを順次読み込む ItemReader。
 * 1 XML ファイル = 1 {@link FinancialStatement} を返す。
 */
@Slf4j
public class KessanjohoXmlReader implements ItemStreamReader<FinancialStatement> {

    private final Path kessanjohoDir;
    private List<Path> xmlFiles;
    private int index = 0;
    private static final DocumentBuilderFactory DOC_BUILDER_FACTORY;

    static {
        DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        // XXE 対策
        try {
            DOC_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public KessanjohoXmlReader(Path kessanjohoDir) {
        this.kessanjohoDir = kessanjohoDir;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try (Stream<Path> stream = Files.walk(kessanjohoDir)) {
            xmlFiles = stream
                .filter(p -> p.toString().endsWith(".xml"))
                .sorted()
                .toList();
        } catch (IOException e) {
            throw new ItemStreamException("Kessanjoho XML ファイル一覧取得失敗: " + kessanjohoDir, e);
        }
        // restart 対応: ExecutionContext に保存された offset から再開
        index = executionContext.getInt("kessanjoho.index", 0);
        log.info("Kessanjoho XMLReader 初期化: {}件, 開始インデックス={}", xmlFiles.size(), index);
    }

    @Override
    public FinancialStatement read() throws Exception {
        while (index < xmlFiles.size()) {
            Path xmlFile = xmlFiles.get(index++);
            log.debug("XML 読み込み: {}", xmlFile.getFileName());
            try {
                return parseXml(xmlFile);
            } catch (Exception e) {
                log.warn("XML パース失敗（スキップ）: {}", xmlFile.getFileName(), e);
                // 次のファイルへ
            }
        }
        return null;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt("kessanjoho.index", index);
    }

    @Override
    public void close() throws ItemStreamException {
        // 特になし
    }

    // ── XML パース ────────────────────────────────────────────────────────────

    private FinancialStatement parseXml(Path xmlFile) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DOC_BUILDER_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(xmlFile.toFile());
        doc.getDocumentElement().normalize();

        FinancialStatement fs = new FinancialStatement();

        // 官報掲載情報
        String status = getTextContent(doc, "Status");
        String keyField = getTextContent(doc, "KeyField");
        fs.setStatus(status);
        fs.setKeyField(keyField);

        // 法人情報
        fs.setCorporateNumber(getTextContent(doc, "CorporateNumber"));
        fs.setCompanyName(getTextContent(doc, "CompanyName"));
        fs.setPeriod(getTextContent(doc, "Period"));
        fs.setReleaseDate(getTextContent(doc, "Release"));
        fs.setUnit(getTextContent(doc, "Unit"));

        // 報告書
        fs.setReports(parseReports(doc));

        return fs;
    }

    private List<FinancialStatement.Report> parseReports(Document doc) {
        List<FinancialStatement.Report> reports = new ArrayList<>();
        NodeList reportNameNodes = doc.getElementsByTagName("ReportName");

        for (int i = 0; i < reportNameNodes.getLength(); i++) {
            Element reportNameEl = (Element) reportNameNodes.item(i);
            FinancialStatement.Report report = new FinancialStatement.Report();

            // 表名属性
            report.setReportName(reportNameEl.getAttribute("表名"));

            // BsPlDate（日付または期間）
            NodeList bsPlDateNodes = reportNameEl.getElementsByTagName("BsPlDate");
            List<FinancialStatement.Division> allDivisions = new ArrayList<>();

            for (int j = 0; j < bsPlDateNodes.getLength(); j++) {
                Element bsPlDateEl = (Element) bsPlDateNodes.item(j);
                String asOfDate = bsPlDateEl.getAttribute("日付");
                if (j == 0) {
                    report.setAsOfDate(asOfDate);
                }
                allDivisions.addAll(parseDivisions(bsPlDateEl));
            }

            report.setDivisions(allDivisions);
            reports.add(report);
        }
        return reports;
    }

    private List<FinancialStatement.Division> parseDivisions(Element parent) {
        List<FinancialStatement.Division> divisions = new ArrayList<>();
        NodeList divisionNodes = parent.getElementsByTagName("Division");

        for (int i = 0; i < divisionNodes.getLength(); i++) {
            Element divEl = (Element) divisionNodes.item(i);
            FinancialStatement.Division division = new FinancialStatement.Division();
            division.setName(divEl.getAttribute("部"));
            division.setItems(parseLineItems(divEl));
            divisions.add(division);
        }
        return divisions;
    }

    private List<FinancialStatement.LineItem> parseLineItems(Element divEl) {
        List<FinancialStatement.LineItem> items = new ArrayList<>();
        NodeList meisaiNodes = divEl.getElementsByTagName("Meisai");

        for (int i = 0; i < meisaiNodes.getLength(); i++) {
            Element meisaiEl = (Element) meisaiNodes.item(i);
            // 直接の子の Meisai のみ（ネストした Division 配下は除く）
            if (!meisaiEl.getParentNode().equals(divEl)) continue;

            FinancialStatement.LineItem item = new FinancialStatement.LineItem();
            item.setSubject(getChildText(meisaiEl, "Subject"));
            item.setAmount(getChildText(meisaiEl, "Amount"));
            items.add(item);
        }
        return items;
    }

    private String getTextContent(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        String text = nodes.item(0).getTextContent();
        return text == null || text.isBlank() ? null : text.trim();
    }
}
