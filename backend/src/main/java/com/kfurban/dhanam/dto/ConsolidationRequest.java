package com.kfurban.dhanam.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Mirrors the parameters the legacy JSP read via request.getParameter(...):
 * rgdt, bkdt, mtmon, brno, dhanamBrNo, rtype, gtype.
 *
 * finYear/finYearCode/finYearStartDate are back (were dropped in the
 * read-only version) because reptdt and dv_locreptandpayment - now revived
 * as the results-storage tables - need them, same as the original JSP.
 */
public class ConsolidationRequest {

    /** Report date, dd-mm-yyyy (was "rgdt"). */
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$", message = "reportDate must be dd-mm-yyyy")
    private String reportDate;

    /** Backup date, dd-mm-yyyy (was "bkdt"). */
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$", message = "backupDate must be dd-mm-yyyy")
    private String backupDate;

    /** Accounting month, 1-12 (was "mtmon"). */
    @NotNull
    private Integer month;

    /** 1 = 12AA, 2 = Mutuals, 3 = AOP (was "brno"). */
    @NotNull
    private Integer branchCategory;

    /** Branch code used to filter almost every query (was "dhanamBrNo"). */
    @NotBlank
    private String branchNo;

    /** 1 Daily, 2 Weekly, 3 Monthly, 4 Annual, 5 CFM, 6 AREAP, 7 HELP (was "rtype"). */
    @NotNull
    private Integer reportType;

    /** 1 Regular groups, 2 Defunct groups, 3 All groups (was "gtype"). */
    @NotNull
    private Integer groupType;

    /**
     * The fiscal year to use as the cutoff for "opening balance as of" date math
     * (was derived from `select year(fromdt) from acctyear`, adjusted +1 if month <= 3).
     * The caller supplies the already-adjusted year directly, e.g. "2026".
     */
    @NotBlank
    private String expectedAccountingYear;

    /** Display financial year, e.g. "2025-2026" (was session "REGFYFY"). Stored into reptdt/dv_locreptandpayment. */
    @NotBlank
    private String finYear;

    /** Financial year code, e.g. "2526" (was session "REGFYFYCODE"). Stored into reptdt/dv_locreptandpayment. */
    @NotBlank
    private String finYearCode;

    /** Financial year start date, dd-mm-yyyy (was session "RFDATE"). Stored into reptdt/dv_locreptandpayment as fromdt. */
    @NotBlank
    @Pattern(regexp = "^\\d{2}-\\d{2}-\\d{4}$", message = "finYearStartDate must be dd-mm-yyyy")
    private String finYearStartDate;

    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }

    public String getBackupDate() { return backupDate; }
    public void setBackupDate(String backupDate) { this.backupDate = backupDate; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getBranchCategory() { return branchCategory; }
    public void setBranchCategory(Integer branchCategory) { this.branchCategory = branchCategory; }

    public String getBranchNo() { return branchNo; }
    public void setBranchNo(String branchNo) { this.branchNo = branchNo; }

    public Integer getReportType() { return reportType; }
    public void setReportType(Integer reportType) { this.reportType = reportType; }

    public Integer getGroupType() { return groupType; }
    public void setGroupType(Integer groupType) { this.groupType = groupType; }

    public String getExpectedAccountingYear() { return expectedAccountingYear; }
    public void setExpectedAccountingYear(String expectedAccountingYear) { this.expectedAccountingYear = expectedAccountingYear; }

    public String getFinYear() { return finYear; }
    public void setFinYear(String finYear) { this.finYear = finYear; }

    public String getFinYearCode() { return finYearCode; }
    public void setFinYearCode(String finYearCode) { this.finYearCode = finYearCode; }

    public String getFinYearStartDate() { return finYearStartDate; }
    public void setFinYearStartDate(String finYearStartDate) { this.finYearStartDate = finYearStartDate; }
}
