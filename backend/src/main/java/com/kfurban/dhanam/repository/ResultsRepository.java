package com.kfurban.dhanam.repository;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kfurban.dhanam.dto.ConsolidationRequest;
import com.kfurban.dhanam.dto.ReportFigures;

/**
 * Writes into the "result" database, using the ORIGINAL table names and
 * column structure from the legacy JSP: reptdt (one summary row per
 * generate() call) and dv_locreptandpayment (one row per account-by-account
 * amount, per location, per side, plus a Total row per location per side).
 *
 * Columns that depended on the removed feddet table (ufc, pcode, rcode,
 * dtcode, dtcodeslno, stcode, ctxt, c1-c6) are written as blank strings -
 * there's no equivalent source for them anymore. fedname/blkname/rname/
 * stname/dtname come from ReportRepository's federation/region/district/
 * state lookups; everything else matches the original insert statements
 * 1:1, including the constant literals (co=1, lvl=1, reptno=1, progconsrno=
 * 'BWR48', ywcomp='Y', chartfinal='Y').
 */
@Repository
public class ResultsRepository {

    private static final String PROGCONSRNAME = "BWR48 - Location wise Receipts and Payments";

    private final JdbcTemplate jdbc;

    public ResultsRepository(@Qualifier("resultsDataSource") DataSource resultsDataSource) {
        this.jdbc = new JdbcTemplate(resultsDataSource);
    }

    /** One summary row per generate() call, matching the original reptdt insert (with a computed recordno). */
    public int insertReptdt(ConsolidationRequest req, String reportDateIso, String backupDateIso,
                             String fromDtIso, String reptGenDateIso, String reportTypeName) {
        Integer maxRecordNo = jdbc.queryForObject("select max(recordno) from reptdt", Integer.class);
        int recordNo = (maxRecordNo == null || maxRecordNo == 0) ? 1 : maxRecordNo + 1;

        jdbc.update(
                "insert into reptdt(recordno, rtype, reptdate, reptgendate, backupdt, progconsrno, " +
                        "progconsrname, finyear, fromdt, ywcomp, chartfinal, finyearcode, brno) " +
                        "values (?, ?, ?, ?, ?, 'BWR48', ?, ?, ?, 'Y', 'Y', ?, ?)",
                recordNo, reportTypeName, reportDateIso, reptGenDateIso, backupDateIso,
                PROGCONSRNAME, req.getFinYear(), fromDtIso, req.getFinYearCode(), req.getBranchNo());

        return recordNo;
    }

    /** One dv_locreptandpayment row for a single non-zero receipt amount (reptahcode/reptahname/reptamt). */
    public void insertReceiptLine(String location, String accountCode, String accountName, long amount,
                                   ReportFigures figures, ConsolidationRequest req, String reportDateIso,
                                   String backupDateIso, String fromDtIso, String reptGenDateIso,
                                   String reportTypeName, String branchLabel) {
        jdbc.update(
                "insert into dv_locreptandpayment(" +
                        "fedname, repttype, reptahcode, reptahname, reptamt, reptdate, co, ctxt, stname, dtname, " +
                        "lvl, reptno, reptgendate, pname, rname, pcode, rcode, c1, c2, c3, c4, c5, c6, ufc, rtype, " +
                        "backupdt, progconsrno, progconsrname, brno, blkname, dtcode, dtcodeslno, stcode, " +
                        "finyear, fromdt, ywcomp, chartfinal, finyearcode, vc70) " +
                        "values (?, 'R', ?, ?, ?, ?, 1, '', ?, ?, 1, 1, ?, '', ?, '', '', '', '', '', '', '', '', " +
                        "'', ?, ?, 'BWR48', ?, ?, ?, '', '', '', ?, ?, 'Y', 'Y', ?, ?)",
                location, accountCode, accountName, amount, reportDateIso,
                figures.getStateName(), figures.getDistrictName(), reptGenDateIso, figures.getRegionName(),
                reportTypeName, backupDateIso, PROGCONSRNAME, branchLabel, location,
                req.getFinYear(), fromDtIso, req.getFinYearCode(), req.getBranchNo());
    }

    /** dv_locreptandpayment "Total" row, receipts side (reptahcode = '9998'). */
    public void insertReceiptTotal(String location, long total, ReportFigures figures, ConsolidationRequest req,
                                    String reportDateIso, String backupDateIso, String fromDtIso,
                                    String reptGenDateIso, String reportTypeName, String branchLabel) {
        jdbc.update(
                "insert into dv_locreptandpayment(" +
                        "fedname, repttype, reptahcode, reptahname, repttotamt, reptdate, co, ctxt, stname, dtname, " +
                        "lvl, reptno, reptgendate, pname, rname, pcode, rcode, c1, c2, c3, c4, c5, c6, ufc, rtype, " +
                        "backupdt, progconsrno, progconsrname, brno, blkname, dtcode, dtcodeslno, stcode, " +
                        "finyear, fromdt, ywcomp, chartfinal, finyearcode, vc70) " +
                        "values (?, 'R', '9998', 'Total', ?, ?, 1, '', ?, ?, 1, 1, ?, '', ?, '', '', '', '', '', '', '', '', " +
                        "'', ?, ?, 'BWR48', ?, ?, ?, '', '', '', ?, ?, 'Y', 'Y', ?, ?)",
                location, total, reportDateIso,
                figures.getStateName(), figures.getDistrictName(), reptGenDateIso, figures.getRegionName(),
                reportTypeName, backupDateIso, PROGCONSRNAME, branchLabel, location,
                req.getFinYear(), fromDtIso, req.getFinYearCode(), req.getBranchNo());
    }

    /** One dv_locreptandpayment row for a single non-zero payment amount (payahcode/payahname/payamt). */
    public void insertPaymentLine(String location, String accountCode, String accountName, long amount,
                                   ReportFigures figures, ConsolidationRequest req, String reportDateIso,
                                   String backupDateIso, String fromDtIso, String reptGenDateIso,
                                   String reportTypeName, String branchLabel) {
        jdbc.update(
                "insert into dv_locreptandpayment(" +
                        "fedname, paytype, payahcode, payahname, payamt, reptdate, co, ctxt, stname, dtname, " +
                        "lvl, reptno, reptgendate, pname, rname, pcode, rcode, c1, c2, c3, c4, c5, c6, ufc, rtype, " +
                        "backupdt, progconsrno, progconsrname, brno, blkname, dtcode, dtcodeslno, stcode, " +
                        "finyear, fromdt, ywcomp, chartfinal, finyearcode, vc70) " +
                        "values (?, 'P', ?, ?, ?, ?, 1, '', ?, ?, 1, 1, ?, '', ?, '', '', '', '', '', '', '', '', " +
                        "'', ?, ?, 'BWR48', ?, ?, ?, '', '', '', ?, ?, 'Y', 'Y', ?, ?)",
                location, accountCode, accountName, amount, reportDateIso,
                figures.getStateName(), figures.getDistrictName(), reptGenDateIso, figures.getRegionName(),
                reportTypeName, backupDateIso, PROGCONSRNAME, branchLabel, location,
                req.getFinYear(), fromDtIso, req.getFinYearCode(), req.getBranchNo());
    }

    /** dv_locreptandpayment "Total" row, payments side (payahcode = '9999'). */
    public void insertPaymentTotal(String location, long total, ReportFigures figures, ConsolidationRequest req,
                                    String reportDateIso, String backupDateIso, String fromDtIso,
                                    String reptGenDateIso, String reportTypeName, String branchLabel) {
        jdbc.update(
                "insert into dv_locreptandpayment(" +
                        "fedname, paytype, payahcode, payahname, paytotamt, reptdate, co, ctxt, stname, dtname, " +
                        "lvl, reptno, reptgendate, pname, rname, pcode, rcode, c1, c2, c3, c4, c5, c6, ufc, rtype, " +
                        "backupdt, progconsrno, progconsrname, brno, blkname, dtcode, dtcodeslno, stcode, " +
                        "finyear, fromdt, ywcomp, chartfinal, finyearcode, vc70) " +
                        "values (?, 'P', '9999', 'Total', ?, ?, 1, '', ?, ?, 1, 1, ?, '', ?, '', '', '', '', '', '', '', '', " +
                        "'', ?, ?, 'BWR48', ?, ?, ?, '', '', '', ?, ?, 'Y', 'Y', ?, ?)",
                location, total, reportDateIso,
                figures.getStateName(), figures.getDistrictName(), reptGenDateIso, figures.getRegionName(),
                reportTypeName, backupDateIso, PROGCONSRNAME, branchLabel, location,
                req.getFinYear(), fromDtIso, req.getFinYearCode(), req.getBranchNo());
    }
}
