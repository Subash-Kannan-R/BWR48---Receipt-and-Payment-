package com.kfurban.dhanam.service;

import com.kfurban.dhanam.config.DhanamDataSources;
import com.kfurban.dhanam.dto.*;
import com.kfurban.dhanam.repository.ReportRepository;
import com.kfurban.dhanam.repository.ResultsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ConsolidationService {

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ReportRepository reportRepository;
    private final ResultsRepository resultsRepository;
    private final DhanamDataSources dataSources;

    public ConsolidationService(ReportRepository reportRepository,
                                 ResultsRepository resultsRepository,
                                 DhanamDataSources dataSources) {
        this.reportRepository = reportRepository;
        this.resultsRepository = resultsRepository;
        this.dataSources = dataSources;
    }

    public ConsolidationResponse generate(ConsolidationRequest req) {
        String reportDateIso = toIso(req.getReportDate());
        String backupDateIso = toIso(req.getBackupDate());
        String fromDtIso = toIso(req.getFinYearStartDate());
        String reportTypeName = reportTypeName(req.getReportType());
        String branchLabel = branchLabel(req.getBranchCategory());

        Map<String, String> receiptAccounts = new LinkedHashMap<>();
        Map<String, String> paymentAccounts = new LinkedHashMap<>();
        List<String> columns = new ArrayList<>();
        List<Map<String, Long>> receiptsPerColumn = new ArrayList<>();
        List<Map<String, Long>> paymentsPerColumn = new ArrayList<>();
        List<Long> receiptTotals = new ArrayList<>();
        List<Long> paymentTotals = new ArrayList<>();
        List<ReportFigures> allFigures = new ArrayList<>();

        for (String schema : dataSources.locationSchemas()) {
            ReportFigures figures;
            try {
                JdbcTemplate jdbc = new JdbcTemplate(dataSources.locationDataSource(schema));
                figures = reportRepository.computeFigures(
                        jdbc, req.getBranchNo(), req.getGroupType(), req.getMonth(), req.getExpectedAccountingYear());
            } catch (Exception ex) {
                // One unreachable/misconfigured schema shouldn't fail the whole report - skip it.
                continue;
            }

            String label = figures.getFederationName().isEmpty() ? schema : figures.getFederationName();
            columns.add(label);
            receiptsPerColumn.add(figures.getReceiptsByAccount());
            paymentsPerColumn.add(figures.getPaymentsByAccount());
            receiptTotals.add(figures.getReceiptTotal());
            paymentTotals.add(figures.getPaymentTotal());
            allFigures.add(figures);

            for (Map.Entry<String, Long> e : figures.getReceiptsByAccount().entrySet()) {
                receiptAccounts.putIfAbsent(e.getKey(), figures.getAccountNames().getOrDefault(e.getKey(), e.getKey()));
            }
            for (Map.Entry<String, Long> e : figures.getPaymentsByAccount().entrySet()) {
                paymentAccounts.putIfAbsent(e.getKey(), figures.getAccountNames().getOrDefault(e.getKey(), e.getKey()));
            }
        }

        List<ReportRow> receiptRows = toRows(receiptAccounts, receiptsPerColumn);
        List<ReportRow> paymentRows = toRows(paymentAccounts, paymentsPerColumn);

        ConsolidationResponse res = new ConsolidationResponse();
        res.setMessage("Report generated successfully — " +
                reportTypeName + " " + groupTypeName(req.getGroupType()) + " for " + branchLabel);
        res.setColumns(columns);
        res.setReceiptRows(receiptRows);
        res.setPaymentRows(paymentRows);
        res.setReceiptTotals(receiptTotals);
        res.setPaymentTotals(paymentTotals);
        res.setReportLevel("Centre");
        res.setLevelName("Central Office");
        res.setGroupByLabel("Block");
        res.setReportTypeLabel(reportTypeName);
        res.setReportDateLabel(req.getReportDate());
        res.setBackupDateLabel(req.getBackupDate());
        res.setReportForLabel("Kalanjiam/AFGs");

        persistToResultDatabase(req, allFigures, columns, receiptRows, paymentRows, receiptTotals, paymentTotals,
                reportDateIso, backupDateIso, fromDtIso, reportTypeName, branchLabel, res);

        return res;
    }

    /**
     * Writes into reptdt (one summary row) and dv_locreptandpayment (every
     * detail line + a Total line, per location, per side) in the `result`
     * database. If that database is unreachable, the report itself still
     * returns successfully - only the storage step is skipped.
     */
    private void persistToResultDatabase(ConsolidationRequest req, List<ReportFigures> allFigures,
                                          List<String> columns, List<ReportRow> receiptRows, List<ReportRow> paymentRows,
                                          List<Long> receiptTotals, List<Long> paymentTotals,
                                          String reportDateIso, String backupDateIso, String fromDtIso,
                                          String reportTypeName, String branchLabel, ConsolidationResponse res) {
        try {
            String reptGenDateIso = LocalDate.now().format(ISO_FORMAT);

            int recordNo = resultsRepository.insertReptdt(req, reportDateIso, backupDateIso, fromDtIso, reptGenDateIso, reportTypeName);
            int lines = 0;

            for (int col = 0; col < columns.size(); col++) {
                String location = columns.get(col);
                ReportFigures figures = allFigures.get(col);

                for (ReportRow row : receiptRows) {
                    long amt = row.getAmounts().get(col);
                    if (amt <= 0) continue;
                    resultsRepository.insertReceiptLine(location, row.getAccountCode(), row.getAccountName(), amt,
                            figures, req, reportDateIso, backupDateIso, fromDtIso, reptGenDateIso, reportTypeName, branchLabel);
                    lines++;
                }
                if (receiptTotals.get(col) > 0) {
                    resultsRepository.insertReceiptTotal(location, receiptTotals.get(col), figures, req,
                            reportDateIso, backupDateIso, fromDtIso, reptGenDateIso, reportTypeName, branchLabel);
                    lines++;
                }

                for (ReportRow row : paymentRows) {
                    long amt = row.getAmounts().get(col);
                    if (amt <= 0) continue;
                    resultsRepository.insertPaymentLine(location, row.getAccountCode(), row.getAccountName(), amt,
                            figures, req, reportDateIso, backupDateIso, fromDtIso, reptGenDateIso, reportTypeName, branchLabel);
                    lines++;
                }
                if (paymentTotals.get(col) > 0) {
                    resultsRepository.insertPaymentTotal(location, paymentTotals.get(col), figures, req,
                            reportDateIso, backupDateIso, fromDtIso, reptGenDateIso, reportTypeName, branchLabel);
                    lines++;
                }
            }

            res.setResultsRunId((long) recordNo);
            res.setResultsLinesStored(lines);
        } catch (Exception ex) {
            // result database unreachable/not created yet - the report result itself is
            // still valid and returned to the caller, just without storage confirmation.
            res.setResultsRunId(null);
            res.setResultsLinesStored(0);
        }
    }

    private List<ReportRow> toRows(Map<String, String> accounts, List<Map<String, Long>> perColumn) {
        List<ReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, String> acc : accounts.entrySet()) {
            ReportRow row = new ReportRow();
            row.setAccountCode(acc.getKey());
            row.setAccountName(acc.getValue());
            for (Map<String, Long> col : perColumn) {
                row.getAmounts().add(col.getOrDefault(acc.getKey(), 0L));
            }
            rows.add(row);
        }
        return rows;
    }

    private String toIso(String ddMmYyyy) {
        return LocalDate.parse(ddMmYyyy, INPUT_FORMAT).format(ISO_FORMAT);
    }

    private String branchLabel(int branchCategory) {
        switch (branchCategory) {
            case 1: return "12AA";
            case 2: return "Mutuals";
            case 3: return "AOP";
            default: throw new IllegalArgumentException("Unknown branch category: " + branchCategory);
        }
    }

    private String reportTypeName(int reportType) {
        switch (reportType) {
            case 1: return "Daily";
            case 2: return "Weekly";
            case 3: return "Monthly";
            case 4: return "Annual";
            case 5: return "CFM";
            case 6: return "AREAP";
            case 7: return "HELP";
            default: throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    private String groupTypeName(int groupType) {
        switch (groupType) {
            case 1: return "Regular Groups";
            case 2: return "Defunct Groups";
            case 3: return "All Groups";
            default: throw new IllegalArgumentException("Unknown group type: " + groupType);
        }
    }
}
