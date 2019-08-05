package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.DefaultBufferedReaderFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import javax.persistence.JoinColumn;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A custom statement ItemReader that interprets and reads a'pipe delimited' file received from XPB.
 *
 * @author F5298334
 */
public class XPBStatementFileReader implements ResourceAwareItemReaderItemStream<XPBStatement> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XPBStatementFileReader.class);
    private static final String resourceDelimiter = "\\|";
    // default encoding for input files
    public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

    private Resource resource;
    private final List<XPBStatement> statementList = new ArrayList<>();
    private BufferedReader reader;
    private String encoding = DEFAULT_CHARSET;
    private BufferedReaderFactory bufferedReaderFactory = new DefaultBufferedReaderFactory();


    private boolean strict;

    @Override
    public XPBStatement read() throws Exception {
        if (!statementList.isEmpty()) {
            return statementList.remove(0);
        }
        return null;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    private void parseResourceAndStoreStatementsToList(Resource resource) {
        LOGGER.info("Parsing Resource {} ", resource.getFilename());
        final List<String> lines = reader.lines().collect(Collectors.toList());
        XPBStatement xpbStatement = null;
        for (String line : lines) {
            String[] lineFields = line.split(resourceDelimiter);
            if (line.startsWith("L|")) {
                xpbStatement = createNewStatement(resource, lines.get(0), xpbStatement, lineFields);
            } else if (line.startsWith("S|") && xpbStatement != null) {
                addSummaryRecord(xpbStatement, lineFields);
            } else if (line.startsWith("X|") && xpbStatement != null) {
                addTaxRecords(xpbStatement, lineFields);
            } else if (line.startsWith("G|") && xpbStatement != null) {
                addGroupingRecord(xpbStatement, lineFields);
            } else if (line.startsWith("O|") && xpbStatement != null) {
                addOtherRecord(xpbStatement, lineFields);
            } else if (line.startsWith("D|") && xpbStatement != null) {
                addDetailedRecord(xpbStatement, lineFields);
            }
        }
        // adds the very last statement before the end of the file.
        addStatementToList(xpbStatement);
        validateTrailerRecordAgainstListOfStatements(resource, lines.get(lines.size() - 1));
    }

    /**
     * Validates the very last line (Trailer record) of the file to ensure that the reader picked up the correct number of
     * statements as the trailer record states.
     *
     * @param resource - The current .dat file
     * @param line     The trailer record. For example "T|48"
     */
    private void validateTrailerRecordAgainstListOfStatements(Resource resource, String line) {
        if (line.startsWith("T|") && Integer.parseInt(line.split(resourceDelimiter)[1]) != statementList.size())
            throw new IllegalArgumentException(
                    "Number of statements imported from file [" + resource.getFilename() + "] does not correspond to trailer record of the file." +
                            " Statement list size : " + statementList.size() + ". Trailer record : " + line);
    }

    private XPBStatement createNewStatement(Resource resource, String resourceHeaderLine, XPBStatement xpbStatement, String[] lineFields) {
        addStatementToList(xpbStatement);
        xpbStatement = new XPBStatement();
        xpbStatement.setFileName(resource.getFilename());
        xpbStatement.setProductionDate(resourceHeaderLine.split(resourceDelimiter)[1]);
        setStatementLayoutFields(xpbStatement, lineFields);
        xpbStatement.setSummaryRecords(new TreeSet<>(Comparator.comparing(SummaryRecord::getOrder)));
        xpbStatement.setTaxRecords(new TreeSet<>(Comparator.comparing(TaxRecord::getOrder)));
        xpbStatement.setGroupingRecords(new TreeSet<>(Comparator.comparing(GroupingRecord::getOrder)));
        xpbStatement.setOtherRecords(new TreeSet<>(Comparator.comparing(OtherRecord::getOrder)));
        xpbStatement.setDetailRecords(new TreeSet<>(Comparator.comparing(DetailRecord::getOrder)));
        return xpbStatement;
    }

    private void addStatementToList(XPBStatement xpbStatement) {
        if (xpbStatement != null) {
            statementList.add(xpbStatement);
        }
    }

    private void addDetailedRecord(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, DetailRecord.class);
        xpbStatement.getDetailRecords().add(DetailRecord.builder()
                .statementNumber(lineFields[1])
                .order(Integer.parseInt(lineFields[2]))
                .transactionDate(transformToLocalDate(lineFields[3]))
                .serviceCode(lineFields[4])
                .serviceDescription(lineFields[5])
                .chargeCode(lineFields[6])
                .chargeDescription(lineFields[7])
                .transactionCurrency(lineFields[8])
                .transactionAmount(new BigDecimal(lineFields[9]))
                .commissionCurrency(lineFields[10])
                .commissionAmount(new BigDecimal(lineFields[11]))
                .build());
    }

    private void addOtherRecord(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, OtherRecord.class);
        xpbStatement.getOtherRecords().add(OtherRecord.builder()
                .statementNumber(lineFields[1])
                .order(Integer.parseInt(lineFields[2]))
                .serviceCode(lineFields[3])
                .serviceDescription(lineFields[4])
                .chargeCode(lineFields[5])
                .chargeDescription(lineFields[6])
                .currency(lineFields[7])
                .amount(new BigDecimal(lineFields[8]))
                .build());

    }

    private void addGroupingRecord(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, GroupingRecord.class);
        xpbStatement.getGroupingRecords().add(GroupingRecord.builder()
                .statementNumber(lineFields[1])
                .order(Integer.parseInt(lineFields[2]))
                .serviceCode(lineFields[3])
                .serviceDescription(lineFields[4])
                .chargeCode(lineFields[5])
                .chargeDescription(lineFields[6])
                .totalNumberOfTransactions(Integer.parseInt(lineFields[7]))
                .transactionCurrency(lineFields[8])
                .totalTransactionValue(new BigDecimal(lineFields[9]))
                .build());
    }

    private void addTaxRecords(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, TaxRecord.class);
        xpbStatement.getTaxRecords().add(TaxRecord.builder()
                .statementNumber(lineFields[1])
                .order(Integer.parseInt(lineFields[2]))
                .type(lineFields[3])
                .currency(lineFields[4])
                .taxAmount(new BigDecimal(lineFields[5]))
                .rate(new BigDecimal(lineFields[6]))
                .build());
    }

    private void addSummaryRecord(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, SummaryRecord.class);
        xpbStatement.getSummaryRecords().add(SummaryRecord.builder()
                .statementNumber(lineFields[1])
                .order(Integer.parseInt(lineFields[2]))
                .type(lineFields[3])
                .currency(lineFields[4])
                .totalAmount(new BigDecimal(lineFields[5]))
                .build());
    }

    private void setStatementLayoutFields(XPBStatement xpbStatement, String[] lineFields) {
        int layoutRecordLength = 7;
        if (lineFields.length != layoutRecordLength)
            throw new IllegalArgumentException("Invalid number of fields picked up from resource. for TYPE -> : " + XPBStatement.class.getSimpleName() + ". LINE -> : "
                    + Arrays.toString(lineFields) + ". Expected " + 7 + ", got " + lineFields.length);
        xpbStatement.setStatementNumber(lineFields[1]);
        xpbStatement.setStatementStartPeriod(transformToLocalDate(lineFields[2]));
        xpbStatement.setStatementEndPeriod(transformToLocalDate(lineFields[3]));
        xpbStatement.setStatementDate(transformToLocalDate(lineFields[4]));
        xpbStatement.setAccountNumber(lineFields[5]);
        xpbStatement.setUniqueCustomerNumber(lineFields[6]);
    }

    private LocalDate transformToLocalDate(String s) {
        int dateLength = 8;//DDMMYYYY
        if (s.length() != dateLength)
            throw new RuntimeException("Invalid Date String : " + s);
        StringBuilder stringBuilder = new StringBuilder(s);
        stringBuilder.insert(dateLength - 4, "-").insert(dateLength - 6, "-");
        String[] split = stringBuilder.toString().split("-");
        return LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));
    }

    private void validateLineNumberOfFields(String[] lineFields, Class aClass) {
        int numberOfFields = Arrays.stream(aClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(JoinColumn.class)).toArray().length + 1;
        if (lineFields.length != numberOfFields)
            throw new IllegalArgumentException("Invalid number of fields picked up from resource. for TYPE -> : " + aClass.getSimpleName() + ". LINE -> : "
                    + Arrays.toString(lineFields) + ". Expected " + numberOfFields + ", got " + lineFields.length);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        LOGGER.info("Opening file... " + this.resource);
        Assert.notNull(resource, "Input resource must be set");

        if (!resource.exists()) {
            LOGGER.warn("Input resource does not exist " + resource.getDescription());
            if (this.strict) {
                throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode): " + this.resource);
            } else {
                LOGGER.warn("Input resource does not exist " + this.resource.getDescription());
            }
        }
        else if (!resource.isReadable()) {
            LOGGER.warn("Input resource is not readable " + resource.getDescription());
            if (this.strict) {
                throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode): " + this.resource);
            } else {
                LOGGER.warn("Input resource is not readable " + this.resource.getDescription());
            }
        }
        try {
            reader = bufferedReaderFactory.create(resource, encoding);
            parseResourceAndStoreStatementsToList(resource);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        LOGGER.info("update executionContext : {}", executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        LOGGER.info("Closing reader....");
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
