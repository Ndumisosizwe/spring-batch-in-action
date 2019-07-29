package com.ndumiso.SpringBatchDemo.config.batch;

import com.ndumiso.SpringBatchDemo.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * A custom statement ItemReader that interprets and reads a'pipe delimited' file received from XPB.
 *
 * @author F5298334
 */
public class XPBStatementFileReader implements ResourceAwareItemReaderItemStream<XPBStatement> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XPBStatementFileReader.class);
    private static final String resourceDelimiter = "\\|";

    private Resource resource;
    private int nextStatementIndex;
    private List<XPBStatement> statementList;

    @Override
    public XPBStatement read() throws Exception {
        XPBStatement nextXPBStatement = null;
        if (nextStatementIndex < statementList.size()) {
            nextXPBStatement = statementList.get(nextStatementIndex);
            nextStatementIndex++;
        }
        return nextXPBStatement;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        LOGGER.info("Reading Resource {} ", resource.getFilename());
        try {
            statementList = new ArrayList<>();
            Scanner statementScanner = new Scanner(resource.getFile());
            String resourceHeaderLine = statementScanner.nextLine();
            LOGGER.info("header line {}", resourceHeaderLine);
            String line = "";
            XPBStatement xpbStatement = null;
            while (statementScanner.hasNextLine()) {
                line = statementScanner.nextLine();
                String[] lineFields = line.split(resourceDelimiter);
                if (line.startsWith("L|")) {
                    xpbStatement = createNewStatement(resource, resourceHeaderLine, xpbStatement, lineFields);
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
            addStatementToChunk(xpbStatement);
            nextStatementIndex = 0;
            validateTrailerRecordAgainstListOfStatements(resource, line);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the very last line (Trailer record) of the file to ensure that the reader picked up the correct number of
     * statements as the trailer record states.
     *
     * @param resource - The current .dat file
     * @param line     The trailer record. For example ""
     */
    private void validateTrailerRecordAgainstListOfStatements(Resource resource, String line) {
        if (line.startsWith("T|") && Integer.parseInt(line.split(resourceDelimiter)[1]) != statementList.size())
            throw new IllegalArgumentException(
                    "Number of statements imported from file [" + resource.getFilename() + "] does not correspond to trailer record of the file." +
                            " Statement list size : " + statementList.size() + ". Trailer record : " + line);
    }

    private XPBStatement createNewStatement(Resource resource, String resourceHeaderLine, XPBStatement xpbStatement, String[] lineFields) {
        addStatementToChunk(xpbStatement);
        xpbStatement = new XPBStatement();
        xpbStatement.setFileName(resource.getFilename());
        xpbStatement.setProductionDate(resourceHeaderLine.split(resourceDelimiter)[1]);
        setLayoutLayoutRecord(xpbStatement, lineFields);
        xpbStatement.setSummaryRecords(new TreeSet<>(Comparator.comparing(SummaryRecord::getOrder)));
        xpbStatement.setTaxRecords(new TreeSet<>(Comparator.comparing(TaxRecord::getOrder)));
        xpbStatement.setGroupingRecords(new TreeSet<>(Comparator.comparing(GroupingRecord::getOrder)));
        xpbStatement.setOtherRecords(new TreeSet<>(Comparator.comparing(OtherRecord::getOrder)));
        xpbStatement.setDetailRecords(new TreeSet<>(Comparator.comparing(DetailRecord::getOrder)));
        return xpbStatement;
    }

    private void addStatementToChunk(XPBStatement xpbStatement) {
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

    private void setLayoutLayoutRecord(XPBStatement xpbStatement, String[] lineFields) {
        validateLineNumberOfFields(lineFields, LayoutRecord.class);
        xpbStatement.setLayoutRecord(LayoutRecord.builder()
                .statementNumber(lineFields[1])
                .statementStartPeriod(transformToLocalDate(lineFields[2]))
                .statementEndPeriod(transformToLocalDate(lineFields[3]))
                .statementDate(transformToLocalDate(lineFields[4]))
                .accountNumber(lineFields[5]).build());
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
        int numberOfFields = aClass.getDeclaredFields().length + 1;
        if (lineFields.length != numberOfFields)
            throw new IllegalArgumentException("Invalid number of fields for " + aClass.getSimpleName() + " : "
                    + Arrays.toString(lineFields) + ". Expected " + numberOfFields + ", got " + lineFields.length);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(resource, "Input resource must be set");

        if (!resource.exists()) {
            LOGGER.warn("Input resource does not exist " + resource.getDescription());
            return;
        }

        if (!resource.isReadable()) {
            LOGGER.warn("Input resource is not readable " + resource.getDescription());
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        LOGGER.info("update executionContext : {}", executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        LOGGER.info("Closing reader....");

    }
}
