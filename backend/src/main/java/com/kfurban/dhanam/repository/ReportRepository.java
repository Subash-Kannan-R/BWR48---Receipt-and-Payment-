package com.kfurban.dhanam.repository;

import com.kfurban.dhanam.dto.ReportFigures;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * All queries needed to compute one location's figures: federation, accthead,
 * grpbalsheet, grpdetail, rcpttran, vouchtran, region, district, state.
 * No dv_locreptandpayment, reptdt, feddet, or acctyear.
 *
 * Stateless - the JdbcTemplate is passed in per call rather than injected,
 * because the service calls this once per location schema, each against a
 * different dynamically-built DataSource (see DhanamDataSources).
 *
 * Every query is parameterized (PreparedStatement via JdbcTemplate).
 */
@Repository
public class ReportRepository {

    /**
     * @param groupType 1 = regular groups only, 2 = defunct groups only, 3 = all groups
     * @param expectedAccountingYear the fiscal year to use for the "opening balance as of" cutoff date
     */
    public ReportFigures computeFigures(JdbcTemplate jdbc, String branchNo, int groupType, int month, String expectedAccountingYear) {
        ReportFigures figures = new ReportFigures();
        String defunctFilter = groupTypeFilter(groupType); // "" (all), "b.defGrp='N'", or "b.defGrp='Y'"

        int yearNum = Integer.parseInt(expectedAccountingYear);
        int nextMonth = month + 1;
        int asOnYear = yearNum;
        if (nextMonth > 12) {
            nextMonth -= 12;
            asOnYear += 1;
        }
        String asOnDate = String.format("%d-%02d-01", asOnYear, nextMonth);

        // ---- federation display name + fedcode (first non-blank fedname, else blkname) ----
        jdbc.query(
                "select fedname, fedcode from federation where fedname != '' and brno = ? and fedname is not null " +
                        "union select blkname, fedcode from federation where (fedname is null or fedname = '') and brno = ?",
                rs -> {
                    if (figures.getFederationName().isEmpty()) {
                        figures.setFederationName(rs.getString(1) == null ? "" : rs.getString(1));
                        figures.setFedCode(rs.getString(2) == null ? "" : rs.getString(2));
                    }
                }, branchNo, branchNo);

        // ---- descriptive region/district/state, best-effort via federation's own rcode/dtcode/stcode ----
        enrichLocationMeta(jdbc, figures, branchNo);

        // ---- account head master (code -> name) ----
        Map<String, String> acctHeadNames = figures.getAccountNames();
        jdbc.query("select ahcode, ahname from accthead where brno = ? order by ahcode",
                rs -> { acctHeadNames.put(rs.getString(1), rs.getString(2)); }, branchNo);

        // ---- opening cash (2111) / opening bank (2112) from grpbalsheet, non-removed groups ----
        long openingCash = nz(jdbc.query(
                "select sum(amt) from grpbalsheet a, grpdetail b where a.grpcode = b.grpcode and b.removed != 'Y' " +
                        "and a.brno = b.brno and a.brno = ? and a.ahCode = '2111'",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo));
        long openingBank = nz(jdbc.query(
                "select sum(amt) from grpbalsheet a, grpdetail b where a.grpcode = b.grpcode and b.removed != 'Y' " +
                        "and a.brno = b.brno and a.brno = ? and a.ahCode = '2112'",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo));

        // ---- running cash/bank totals up to the "as-on" cutoff and the selected month ----
        long beforePeriodReceiptCash = nz(jdbc.query(
                "select sum(amt) from rcpttran b, grpdetail c where b.grpcode = c.grpcode and c.removed != 'Y' " +
                        "and b.brno = c.brno and b.brno = ? and rtype = 2 and rcptdt < ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, asOnDate));

        long cashRunningTotal = 0L;
        long bankRunningTotal = beforePeriodReceiptCash;

        String groupJoin = "grpdetail b where a.grpcode = b.grpcode and b.removed != 'Y' and a.brno = b.brno and a.brno = ?" +
                (defunctFilter.isEmpty() ? "" : " and " + defunctFilter);

        long receiptsToMonth = nz(jdbc.query(
                "select sum(amt) from rcpttran a, " + groupJoin + " and month <= ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, month));
        cashRunningTotal = receiptsToMonth - beforePeriodReceiptCash;

        long vouchersBeforePeriod = nz(jdbc.query(
                "select sum(amt) from vouchtran b, grpdetail c where b.grpcode = c.grpcode and c.removed != 'Y' " +
                        "and b.brno = c.brno and b.brno = ? and vtype = 2 and vouchdt < ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, asOnDate));
        cashRunningTotal += vouchersBeforePeriod;
        bankRunningTotal -= vouchersBeforePeriod;

        long vouchersToMonth = nz(jdbc.query(
                "select sum(amt) from vouchtran a, " + groupJoin + " and month <= ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, month));
        cashRunningTotal -= vouchersToMonth;

        long closingCash = openingCash + cashRunningTotal;

        long receiptsBankToMonth = nz(jdbc.query(
                "select sum(amt) from rcpttran a, " + groupJoin + " and ahcode = '2112' and month <= ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, month));
        bankRunningTotal -= receiptsBankToMonth;

        long vouchersBankToMonth = nz(jdbc.query(
                "select sum(amt) from vouchtran a, " + groupJoin + " and ahcode = '2112' and month <= ?",
                rs -> rs.next() ? rs.getObject(1) : null, branchNo, month));
        bankRunningTotal += vouchersBankToMonth;

        long closingBank = openingBank + bankRunningTotal;

        // ---- receipts side: opening cash + opening bank, then every other account head up to `month` ----
        Map<String, Long> receipts = figures.getReceiptsByAccount();
        receipts.merge("2111", openingCash, Long::sum);
        receipts.merge("2112", openingBank, Long::sum);
        figures.setReceiptTotal(openingCash + openingBank);

        jdbc.query("select ahcode, sum(amt) from rcpttran a, " + groupJoin +
                        " and month <= ? and ahcode != '2112' group by ahcode order by ahcode",
                rs -> {
                    String code = rs.getString(1);
                    long amt = rs.getLong(2);
                    receipts.merge(code, amt, Long::sum);
                    figures.setReceiptTotal(figures.getReceiptTotal() + amt);
                }, branchNo, month);

        // ---- payments side: every account head up to `month`, then closing cash + closing bank ----
        Map<String, Long> payments = figures.getPaymentsByAccount();
        jdbc.query("select ahcode, sum(amt) from vouchtran a, " + groupJoin +
                        " and month <= ? and ahcode != '2112' group by ahcode order by ahcode",
                rs -> {
                    String code = rs.getString(1);
                    long amt = rs.getLong(2);
                    payments.merge(code, amt, Long::sum);
                    figures.setPaymentTotal(figures.getPaymentTotal() + amt);
                }, branchNo, month);

        payments.merge("2111", closingCash, Long::sum);
        payments.merge("2112", closingBank, Long::sum);
        figures.setPaymentTotal(figures.getPaymentTotal() + closingCash + closingBank);

        return figures;
    }

    /**
     * Best-effort descriptive lookup. Assumes `federation` carries its own
     * rcode/dtcode/stcode columns. Wrapped in try/catch so a wrong assumption
     * just leaves region/district/state blank instead of failing the report.
     */
    private void enrichLocationMeta(JdbcTemplate jdbc, ReportFigures figures, String branchNo) {
        try {
            String[] codes = jdbc.query(
                    "select rcode, dtcode, stcode from federation where fedcode = ? and brno = ? limit 1",
                    rs -> rs.next() ? new String[]{rs.getString(1), rs.getString(2), rs.getString(3)} : null,
                    figures.getFedCode(), branchNo);

            if (codes == null) return;

            figures.setRcode(codes[0] == null ? "" : codes[0]);
            figures.setDtcode(codes[1] == null ? "" : codes[1]);
            figures.setStcode(codes[2] == null ? "" : codes[2]);

            if (codes[0] != null) {
                jdbc.query("select rname from region where rcode = ? and removed != 'Y'",
                        rs -> { figures.setRegionName(rs.getString(1)); }, codes[0]);
            }
            if (codes[1] != null) {
                jdbc.query("select distname from district where dtcode = ?",
                        rs -> { figures.setDistrictName(rs.getString(1)); }, codes[1]);
            }
            if (codes[2] != null) {
                jdbc.query("select stname from state where stcode = ?",
                        rs -> { figures.setStateName(rs.getString(1)); }, codes[2]);
            }
        } catch (Exception ex) {
            // federation doesn't carry rcode/dtcode/stcode, or region/district/state
            // schemas differ from what we assumed - degrade gracefully.
        }
    }

    private String groupTypeFilter(int groupType) {
        if (groupType == 1) return "b.defGrp = 'N'";
        if (groupType == 2) return "b.defGrp = 'Y'";
        return "";
    }

    private long nz(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
}
