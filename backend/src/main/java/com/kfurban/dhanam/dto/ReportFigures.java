package com.kfurban.dhanam.dto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Everything computed for the branch's federation: opening cash/bank,
 * receipts/payments by account head up to the selected month, and closing
 * cash/bank folded into the payments side. (Was one iteration of the JSP's
 * "for (int i = 0; i < size; i++)" loop; now there's exactly one schema, so
 * exactly one of these per request.)
 */
public class ReportFigures {

    /** Federation / block display name (was "blk[i]"). */
    private String federationName = "";

    /** Federation code, used only to look up region/district/state, not persisted anywhere. */
    private String fedCode = "";

    private Map<String, Long> receiptsByAccount = new LinkedHashMap<>();
    private Map<String, Long> paymentsByAccount = new LinkedHashMap<>();
    private Map<String, String> accountNames = new LinkedHashMap<>();

    private long receiptTotal = 0L;
    private long paymentTotal = 0L;

    private String regionName = "";
    private String districtName = "";
    private String stateName = "";

    /** Raw codes (from federation.rcode/dtcode/stcode), needed for the dv_locreptandpayment insert. */
    private String rcode = "";
    private String dtcode = "";
    private String stcode = "";

    public String getRcode() { return rcode; }
    public void setRcode(String rcode) { this.rcode = rcode; }

    public String getDtcode() { return dtcode; }
    public void setDtcode(String dtcode) { this.dtcode = dtcode; }

    public String getStcode() { return stcode; }
    public void setStcode(String stcode) { this.stcode = stcode; }

    public String getFederationName() { return federationName; }
    public void setFederationName(String federationName) { this.federationName = federationName; }

    public String getFedCode() { return fedCode; }
    public void setFedCode(String fedCode) { this.fedCode = fedCode; }

    public Map<String, Long> getReceiptsByAccount() { return receiptsByAccount; }
    public void setReceiptsByAccount(Map<String, Long> receiptsByAccount) { this.receiptsByAccount = receiptsByAccount; }

    public Map<String, Long> getPaymentsByAccount() { return paymentsByAccount; }
    public void setPaymentsByAccount(Map<String, Long> paymentsByAccount) { this.paymentsByAccount = paymentsByAccount; }

    public Map<String, String> getAccountNames() { return accountNames; }
    public void setAccountNames(Map<String, String> accountNames) { this.accountNames = accountNames; }

    public long getReceiptTotal() { return receiptTotal; }
    public void setReceiptTotal(long receiptTotal) { this.receiptTotal = receiptTotal; }

    public long getPaymentTotal() { return paymentTotal; }
    public void setPaymentTotal(long paymentTotal) { this.paymentTotal = paymentTotal; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }

    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }
}
