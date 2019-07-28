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
 * A custom ItemReader that interprets and reads XPB statement pipe delimited files.
 *
 * @author F5298334
 */
public class XPBStatementFileReader implements
        ResourceAwareItemReaderItemStream<XPBStatement> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XPBStatementFileReader.class);

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
            String line;
            XPBStatement xpbStatement = null;
            Set<SummaryRecord> summaryRecords = null;
            Set<TaxRecord> taxRecords = null;
            Set<GroupingRecord> groupingRecords = null;

            while (statementScanner.hasNextLine()) {
                line = statementScanner.nextLine();
                String[] splitLine = line.split("\\|");
                if (line.startsWith("L|")) {
                    finalizeAndAddNewStatement(xpbStatement, summaryRecords, taxRecords, groupingRecords);
                    xpbStatement = new XPBStatement();
                    summaryRecords = new TreeSet<>(Comparator.comparing(SummaryRecord::getOrder));
                    taxRecords = new TreeSet<>(Comparator.comparing(TaxRecord::getOrder));
                    groupingRecords = new TreeSet<>(Comparator.comparing(GroupingRecord::getOrder));
                    xpbStatement.setFileName(resource.getFilename());
                    xpbStatement.setProductionDate(resourceHeaderLine.split("\\|")[1]);
                    setLayoutLayoutRecord(xpbStatement, splitLine);
                } else if (line.startsWith("S|") && xpbStatement != null) {
                    addSummaryRecord(summaryRecords, splitLine);
                } else if (line.startsWith("X|") && xpbStatement != null) {
                    addTaxRecords(taxRecords, splitLine);
                } else if (line.startsWith("G|") && xpbStatement != null) {
                    addGroupingRecord(groupingRecords, splitLine);
                }
            }
            // adds the very last statement that was read in the file.
            finalizeAndAddNewStatement(xpbStatement, summaryRecords, taxRecords, groupingRecords);
            nextStatementIndex = 0;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void finalizeAndAddNewStatement(XPBStatement xpbStatement, Set<SummaryRecord> summaryRecords, Set<TaxRecord> taxRecords,
                                            Set<GroupingRecord> groupingRecords) {
        if (xpbStatement != null) {
            xpbStatement.setSummaryRecords(summaryRecords);
            xpbStatement.setTaxRecords(taxRecords);
            xpbStatement.setGroupingRecords(groupingRecords);
            statementList.add(xpbStatement);
        }
    }

    private void addGroupingRecord(Set<GroupingRecord> groupingRecords, String[] splitLine) {
        if (splitLine.length != 10)
            throw new IllegalArgumentException("Invalid number of fields for grouping (G) record : "
                    + Arrays.toString(splitLine) + ". Expected 10, got " + splitLine.length);
        groupingRecords.add(GroupingRecord.builder()
                .statementNumber(splitLine[1])
                .order(Integer.parseInt(splitLine[2]))
                .serviceCode(splitLine[3])
                .serviceDescription(splitLine[4])
                .chargeCode(splitLine[5])
                .chargeDescription(splitLine[6])
                .totalNumberOfTransactions(Integer.parseInt(splitLine[7]))
                .transactionCurrency(splitLine[8])
                .totalTransactionValue(new BigDecimal(splitLine[9]))
                .build());
    }

    private void addTaxRecords(Set<TaxRecord> taxRecords, String[] splitLine) {
        if (splitLine.length != 7)
            throw new IllegalArgumentException("Invalid number of fields for tax (X) record : "
                    + Arrays.toString(splitLine) + ". Expected 7, got " + splitLine.length);
        taxRecords.add(TaxRecord.builder()
                .statementNumber(splitLine[1])
                .order(Integer.parseInt(splitLine[2]))
                .type(splitLine[3])
                .currency(splitLine[4])
                .taxAmount(new BigDecimal(splitLine[5]))
                .rate(new BigDecimal(splitLine[6]))
                .build());
    }

    private void addSummaryRecord(Set<SummaryRecord> summaryRecords, String[] splitLine) {
        if (splitLine.length != 6)
            throw new IllegalArgumentException("Invalid number of fields for summary record (S) : "
                    + Arrays.toString(splitLine) + ". Expected 6, got " + splitLine.length);
        summaryRecords.add(SummaryRecord.builder()
                .statementNumber(splitLine[1])
                .order(Integer.parseInt(splitLine[2]))
                .type(splitLine[3])
                .currency(splitLine[4])
                .totalAmount(new BigDecimal(splitLine[5]))
                .build());
    }

    private void setLayoutLayoutRecord(XPBStatement xpbStatement, String[] splitLine) {
        if (splitLine.length != 6)
            throw new IllegalArgumentException("Invalid number of fields for layout record (L) : "
                    + Arrays.toString(splitLine) + ". Expected 6, got " + splitLine.length);
        xpbStatement.setLayoutRecord(LayoutRecord.builder()
                .statementNumber(splitLine[1])
                .statementStartPeriod(transformToLocalDate(splitLine[2]))
                .statementEndPeriod(transformToLocalDate(splitLine[3]))
                .statementDate(transformToLocalDate(splitLine[4]))
                .accountNumber(splitLine[5]).build());
    }

    private LocalDate transformToLocalDate(String s) {
        if (s.length() != 8)
            throw new RuntimeException("Invalid Date String : " + s);
        StringBuilder stringBuilder = new StringBuilder(s);
        stringBuilder.insert(s.length() - 4, "-").insert(s.length() - 6, "-");
        String[] split = stringBuilder.toString().split("-");
        return LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));
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
        LOGGER.info("update executionContext.... {} :", executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        LOGGER.info("Closing reader.... :");

    }
}
