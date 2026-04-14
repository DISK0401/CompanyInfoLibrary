package com.companylib.batch.job;

import com.companylib.batch.infrastructure.gbizinfo.CompanyFullUpsertService;
import com.companylib.batch.infrastructure.gbizinfo.GBizInfoDeserializationProblemHandler;
import com.companylib.batch.infrastructure.gbizinfo.KessanjohoXmlReader;
import com.companylib.batch.infrastructure.gbizinfo.KihonjohoFieldSetMapper;
import com.companylib.batch.infrastructure.gbizinfo.download.DownloadFileType;
import com.companylib.batch.infrastructure.gbizinfo.download.GBizInfoDownloadService;
import com.companylib.batch.infrastructure.gbizinfo.dto.FinancialStatement;
import com.companylib.batch.infrastructure.gbizinfo.dto.HojinInfo;
import com.companylib.batch.infrastructure.gbizinfo.dto.KihonjohoRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * gBizINFO 初回データ投入ジョブ。
 *
 * <p>処理フロー:
 * <ol>
 *   <li>downloadHojinjohoStep  — Hojinjoho ZIP のダウンロード・解凍</li>
 *   <li>importHojinjohoStep    — Hojinjoho JSON → 全テーブル UPSERT</li>
 *   <li>downloadKessanjohoStep — Kessanjoho ZIP のダウンロード・解凍</li>
 *   <li>importKessanjohoStep   — Kessanjoho XML → company_financial_statements UPSERT</li>
 * </ol>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GBizInfoInitialLoadJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GBizInfoDownloadService downloadService;
    private final CompanyFullUpsertService companyFullUpsertService;
    private final ObjectMapper objectMapper;

    public static final String JOB_NAME = "gbizInfoInitialLoadJob";

    /** Hojinjoho チャンクサイズ（1 件 = 1 企業の全データ） */
    private static final int HOJINJOHO_CHUNK_SIZE = 500;

    /** Kihonjoho チャンクサイズ（CSV 1 行 = 1 企業の基本情報のみ） */
    private static final int KIHONJOHO_CHUNK_SIZE = 2000;

    /** Kessanjoho チャンクサイズ（1 件 = 1 XML ファイル） */
    private static final int KESSANJOHO_CHUNK_SIZE = 200;

    // ── Job ──────────────────────────────────────────────────────────────────

    @Bean(JOB_NAME)
    public Job gbizInfoInitialLoadJob(
        Step downloadHojinjohoStep,
        Step importHojinjohoStep,
        Step downloadKihonjohoStep,
        Step importKihonjohoStep,
        Step downloadKessanjohoStep,
        Step importKessanjohoStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(downloadHojinjohoStep)   // Step 1: Hojinjoho DL
            .next(importHojinjohoStep)      // Step 2: 434K件・詳細データ投入
            .next(downloadKihonjohoStep)    // Step 3: Kihonjoho DL
            .next(importKihonjohoStep)      // Step 4: 残り~537万件の基本情報を補完（INSERT ONLY）
            .next(downloadKessanjohoStep)   // Step 5: Kessanjoho DL
            .next(importKessanjohoStep)     // Step 6: 決算情報
            .build();
    }

    // ── Step 1: Hojinjoho ダウンロード ──────────────────────────────────────

    @Bean
    public Step downloadHojinjohoStep() {
        return new StepBuilder("downloadHojinjohoStep", jobRepository)
            .tasklet(downloadHojinjohoTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet downloadHojinjohoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== Step 1/6: Hojinjoho ZIP ダウンロード開始 ===");
            Path extractedDir = downloadService.downloadAndExtract(DownloadFileType.HOJINJOHO);
            // 解凍先パスを Step 2 に引き渡す
            chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putString("hojinjohoExtractedDir", extractedDir.toAbsolutePath().toString());
            log.info("=== Step 1/6: Hojinjoho ZIP ダウンロード完了 → {} ===", extractedDir);
            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: Kihonjoho ダウンロード ──────────────────────────────────────

    @Bean
    public Step downloadKihonjohoStep() {
        return new StepBuilder("downloadKihonjohoStep", jobRepository)
            .tasklet(downloadKihonjohoTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet downloadKihonjohoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== Step 3/6: Kihonjoho ZIP ダウンロード開始 ===");
            Path extractedDir = downloadService.downloadAndExtract(DownloadFileType.KIHONJOHO);
            chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putString("kihonjohoExtractedDir", extractedDir.toAbsolutePath().toString());
            log.info("=== Step 3/6: Kihonjoho ZIP ダウンロード完了 → {} ===", extractedDir);
            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 3: Kihonjoho インポート ─────────────────────────────────────────

    @Bean
    public Step importKihonjohoStep(FlatFileItemReader<KihonjohoRow> kihonjohoReader) {
        return new StepBuilder("importKihonjohoStep", jobRepository)
            .<KihonjohoRow, KihonjohoRow>chunk(KIHONJOHO_CHUNK_SIZE, transactionManager)
            .reader(kihonjohoReader)
            .processor(kihonjohoProcessor())
            .writer(kihonjohoWriter())
            .faultTolerant()
            .skipLimit(50000)
            .skip(Exception.class)
            .noSkip(org.springframework.dao.DataAccessResourceFailureException.class)
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("=== Step 4/6: Kihonjoho インポート開始 ===");
                }
                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("=== Step 4/6: Kihonjoho インポート完了: 読込={}, 書込={}, スキップ={} ===",
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount());
                    return stepExecution.getExitStatus();
                }
            })
            .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<KihonjohoRow> kihonjohoReader(
        @Value("#{jobExecutionContext['kihonjohoExtractedDir']}") String extractedDir
    ) throws IOException {
        Path csvFile = findCsvFile(extractedDir, "Kihonjoho");
        log.info("Kihonjoho CSV ファイル: {}", csvFile);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');

        DefaultLineMapper<KihonjohoRow> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new KihonjohoFieldSetMapper());

        FlatFileItemReader<KihonjohoRow> reader = new FlatFileItemReader<>();
        reader.setName("kihonjohoReader");
        reader.setResource(new FileSystemResource(csvFile));
        reader.setEncoding("UTF-8");
        reader.setLinesToSkip(1);  // ヘッダー行をスキップ（BOM は先頭行に含まれるため自動消費）
        reader.setLineMapper(lineMapper);
        // BOM 付き UTF-8 対応: 先頭 3 バイト (0xEF 0xBB 0xBF) を検出してスキップ
        reader.setBufferedReaderFactory((resource, encoding) -> {
            java.io.InputStream is = resource.getInputStream();
            java.io.PushbackInputStream pis = new java.io.PushbackInputStream(is, 3);
            byte[] bom = new byte[3];
            int bytesRead = pis.read(bom, 0, 3);
            if (bytesRead == 3
                && (bom[0] & 0xFF) == 0xEF
                && (bom[1] & 0xFF) == 0xBB
                && (bom[2] & 0xFF) == 0xBF) {
                log.debug("UTF-8 BOM を検出・スキップしました");
            } else if (bytesRead > 0) {
                pis.unread(bom, 0, bytesRead);
            }
            return new java.io.BufferedReader(new java.io.InputStreamReader(pis, encoding));
        });
        return reader;
    }

    /**
     * 解凍ディレクトリを再帰的に検索して指定プレフィックスの CSV ファイルを返す。
     * 例: extractedDir/Kihonjoho_UTF-8_20260413/Kihonjoho_UTF-8.csv
     */
    private Path findCsvFile(String extractedDir, String prefix) throws IOException {
        try (Stream<Path> stream = Files.walk(Path.of(extractedDir))) {
            return stream
                .filter(p -> p.getFileName().toString().startsWith(prefix)
                    && p.getFileName().toString().endsWith(".csv"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "CSV ファイルが見つかりません: prefix=" + prefix + ", dir=" + extractedDir));
        }
    }

    @Bean
    public ItemProcessor<KihonjohoRow, KihonjohoRow> kihonjohoProcessor() {
        return row -> {
            if (row.getCorporateNumber() == null || row.getCorporateNumber().isBlank()) {
                log.debug("法人番号欠損のためスキップ");
                return null;
            }
            if (row.getName() == null || row.getName().isBlank()) {
                log.debug("法人名欠損のためスキップ: corporateNumber={}", row.getCorporateNumber());
                return null;
            }
            return row;
        };
    }

    @Bean
    public ItemWriter<KihonjohoRow> kihonjohoWriter() {
        return items -> {
            int inserted = 0;
            int skipped = 0;
            for (KihonjohoRow row : items) {
                if (companyFullUpsertService.upsertFromKihonjoho(row) > 0) {
                    inserted++;
                } else {
                    skipped++;
                }
            }
            log.debug("Kihonjoho チャンク処理: 新規={}, スキップ（Hojinjoho 登録済）={}", inserted, skipped);
        };
    }

    // ── Step 4: Hojinjoho インポート ───────────────────────────────────────

    @Bean
    public Step importHojinjohoStep(
        SynchronizedItemStreamReader<HojinInfo> hojinjohoMultiReader
    ) {
        return new StepBuilder("importHojinjohoStep", jobRepository)
            .<HojinInfo, HojinInfo>chunk(HOJINJOHO_CHUNK_SIZE, transactionManager)
            .reader(hojinjohoMultiReader)
            .processor(hojinjohoProcessor())
            .writer(hojinjohoWriter())
            .faultTolerant()
            .skipLimit(5000)
            .skip(com.fasterxml.jackson.core.JsonProcessingException.class)
            .skip(IllegalArgumentException.class)
            .noSkip(org.springframework.dao.DataAccessException.class)
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("=== Step 2/6: Hojinjoho インポート開始 ===");
                }
                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("=== Step 2/6: Hojinjoho インポート完了: 読込={}, 書込={}, スキップ={} ===",
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount());
                    return stepExecution.getExitStatus();
                }
            })
            .build();
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<HojinInfo> hojinjohoMultiReader(
        @Value("#{jobExecutionContext['hojinjohoExtractedDir']}") String extractedDir
    ) throws IOException {
        var resources = new PathMatchingResourcePatternResolver()
            .getResources("file:" + extractedDir + "/**/*.json");

        if (resources.length == 0) {
            throw new IllegalStateException("Hojinjoho JSON ファイルが見つかりません: " + extractedDir);
        }

        // ファイル名順にソート（Hojinjoho_01.json, _02.json, ...）
        Arrays.sort(resources, Comparator.comparing(r -> r.getFilename() == null ? "" : r.getFilename()));

        log.info("Hojinjoho JSON ファイル数: {}", resources.length);

        // 未知フィールドを WARN ログで継続するハンドラを設定した ObjectMapper を使用
        ObjectMapper readerMapper = objectMapper.copy()
            .addHandler(new GBizInfoDeserializationProblemHandler());

        JsonItemReader<HojinInfo> delegate = new JsonItemReaderBuilder<HojinInfo>()
            .name("hojinjohoJsonDelegate")
            .jsonObjectReader(new JacksonJsonObjectReader<>(readerMapper, HojinInfo.class))
            .build();

        var multiReader = new org.springframework.batch.item.file.MultiResourceItemReader<HojinInfo>();
        multiReader.setName("hojinjohoMultiReader");
        multiReader.setResources(resources);
        multiReader.setDelegate(delegate);

        return new SynchronizedItemStreamReaderBuilder<HojinInfo>()
            .delegate(multiReader)
            .build();
    }

    @Bean
    public ItemProcessor<HojinInfo, HojinInfo> hojinjohoProcessor() {
        return item -> {
            if (item.getCorporateNumber() == null || item.getCorporateNumber().isBlank()) {
                log.debug("法人番号欠損のためスキップ");
                return null;
            }
            if (item.getName() == null || item.getName().isBlank()) {
                log.debug("法人名欠損のためスキップ: corporateNumber={}", item.getCorporateNumber());
                return null;
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<HojinInfo> hojinjohoWriter() {
        return items -> {
            for (HojinInfo item : items) {
                companyFullUpsertService.upsert(item);
            }
        };
    }

    // ── Step 3: Kessanjoho ダウンロード ────────────────────────────────────

    @Bean
    public Step downloadKessanjohoStep() {
        return new StepBuilder("downloadKessanjohoStep", jobRepository)
            .tasklet(downloadKessanjohoTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet downloadKessanjohoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== Step 5/6: Kessanjoho ZIP ダウンロード開始 ===");
            Path extractedDir = downloadService.downloadAndExtract(DownloadFileType.KESSANJOHO);
            chunkContext.getStepContext().getStepExecution()
                .getJobExecution().getExecutionContext()
                .putString("kessanjohoExtractedDir", extractedDir.toAbsolutePath().toString());
            log.info("=== Step 5/6: Kessanjoho ZIP ダウンロード完了 → {} ===", extractedDir);
            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 4: Kessanjoho インポート ──────────────────────────────────────

    @Bean
    public Step importKessanjohoStep(
        KessanjohoXmlReader kessanjohoXmlReader
    ) {
        return new StepBuilder("importKessanjohoStep", jobRepository)
            .<FinancialStatement, FinancialStatement>chunk(KESSANJOHO_CHUNK_SIZE, transactionManager)
            .reader(kessanjohoXmlReader)
            .processor(kessanjohoProcessor())
            .writer(kessanjohoWriter())
            .faultTolerant()
            .skipLimit(10000)
            .skip(Exception.class)
            // DB接続障害等の重大エラーは失敗させる
            .noSkip(org.springframework.dao.DataAccessResourceFailureException.class)
            // FK違反（companies テーブルに存在しない法人番号）はスキップしてログ出力
            .skip(org.springframework.dao.DataIntegrityViolationException.class)
            .listener(new org.springframework.batch.core.SkipListener<FinancialStatement, FinancialStatement>() {
                @Override
                public void onSkipInWrite(FinancialStatement item, Throwable t) {
                    if (t instanceof org.springframework.dao.DataIntegrityViolationException) {
                        log.warn("決算情報スキップ（法人番号が companies テーブルに未登録）: "
                            + "corporateNumber={}, period={}",
                            item.getCorporateNumber(), item.getPeriod());
                    } else {
                        log.warn("決算情報スキップ（書込エラー）: corporateNumber={}, period={}, error={}",
                            item.getCorporateNumber(), item.getPeriod(), t.getMessage());
                    }
                }
            })
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("=== Step 6/6: Kessanjoho インポート開始 ===");
                }
                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("=== Step 6/6: Kessanjoho インポート完了: 読込={}, 書込={}, スキップ={} ===",
                        stepExecution.getReadCount(),
                        stepExecution.getWriteCount(),
                        stepExecution.getSkipCount());
                    return stepExecution.getExitStatus();
                }
            })
            .build();
    }

    @Bean
    @StepScope
    public KessanjohoXmlReader kessanjohoXmlReader(
        @Value("#{jobExecutionContext['kessanjohoExtractedDir']}") String extractedDir
    ) {
        log.info("Kessanjoho 解凍ディレクトリ: {}", extractedDir);
        return new KessanjohoXmlReader(Path.of(extractedDir));
    }

    @Bean
    public ItemProcessor<FinancialStatement, FinancialStatement> kessanjohoProcessor() {
        return item -> {
            if (item.getCorporateNumber() == null || item.getKeyField() == null) {
                log.debug("必須フィールド欠損のためスキップ: period={}", item.getPeriod());
                return null;
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<FinancialStatement> kessanjohoWriter() {
        return items -> {
            for (FinancialStatement item : items) {
                companyFullUpsertService.upsertFinancialStatement(item);
            }
        };
    }
}
