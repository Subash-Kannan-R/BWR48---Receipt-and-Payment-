package com.kfurban.dhanam.dto;

import java.util.ArrayList;
import java.util.List;

public class ReportRow {
    private String accountCode;
    private String accountName;
    /** One amount per location column, same order/length as ConsolidationResponse.columns. */
    private List<Long> amounts = new ArrayList<>();

    public ReportRow() { }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public List<Long> getAmounts() { return amounts; }
    public void setAmounts(List<Long> amounts) { this.amounts = amounts; }
}
