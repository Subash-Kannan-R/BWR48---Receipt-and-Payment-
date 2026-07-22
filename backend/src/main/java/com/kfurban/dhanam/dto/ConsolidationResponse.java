package com.kfurban.dhanam.dto;

import java.util.ArrayList;
import java.util.List;

public class ConsolidationResponse {

    private String message;

    /** One column per federation found for this branch (normally just one). */
    private List<String> columns = new ArrayList<>();

    private List<ReportRow> receiptRows = new ArrayList<>();
    private List<ReportRow> paymentRows = new ArrayList<>();

    private List<Long> receiptTotals = new ArrayList<>();
    private List<Long> paymentTotals = new ArrayList<>();

    /** Descriptive metadata for the branch's federation, best-effort (blank if not resolvable). */
    private String regionName = "";
    private String districtName = "";
    private String stateName = "";

    /** Report header strip, matching the BWR48 report layout (Report Level / Level Name / Group By / etc.). */
    private String reportLevel = "";
    private String levelName = "";
    private String groupByLabel = "";
    private String reportTypeLabel = "";
    private String reportDateLabel = "";
    private String backupDateLabel = "";
    private String reportForLabel = "";

    /** Set once the summary row has been written to reptdt (its recordno); null if storage failed or was skipped. */
    private Long resultsRunId;
    private int resultsLinesStored;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<ReportRow> getReceiptRows() { return receiptRows; }
    public void setReceiptRows(List<ReportRow> receiptRows) { this.receiptRows = receiptRows; }

    public List<ReportRow> getPaymentRows() { return paymentRows; }
    public void setPaymentRows(List<ReportRow> paymentRows) { this.paymentRows = paymentRows; }

    public List<Long> getReceiptTotals() { return receiptTotals; }
    public void setReceiptTotals(List<Long> receiptTotals) { this.receiptTotals = receiptTotals; }

    public List<Long> getPaymentTotals() { return paymentTotals; }
    public void setPaymentTotals(List<Long> paymentTotals) { this.paymentTotals = paymentTotals; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }

    public String getReportLevel() { return reportLevel; }
    public void setReportLevel(String reportLevel) { this.reportLevel = reportLevel; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getGroupByLabel() { return groupByLabel; }
    public void setGroupByLabel(String groupByLabel) { this.groupByLabel = groupByLabel; }

    public String getReportTypeLabel() { return reportTypeLabel; }
    public void setReportTypeLabel(String reportTypeLabel) { this.reportTypeLabel = reportTypeLabel; }

    public String getReportDateLabel() { return reportDateLabel; }
    public void setReportDateLabel(String reportDateLabel) { this.reportDateLabel = reportDateLabel; }

    public String getBackupDateLabel() { return backupDateLabel; }
    public void setBackupDateLabel(String backupDateLabel) { this.backupDateLabel = backupDateLabel; }

    public String getReportForLabel() { return reportForLabel; }
    public void setReportForLabel(String reportForLabel) { this.reportForLabel = reportForLabel; }

    public Long getResultsRunId() { return resultsRunId; }
    public void setResultsRunId(Long resultsRunId) { this.resultsRunId = resultsRunId; }

    public int getResultsLinesStored() { return resultsLinesStored; }
    public void setResultsLinesStored(int resultsLinesStored) { this.resultsLinesStored = resultsLinesStored; }
}
