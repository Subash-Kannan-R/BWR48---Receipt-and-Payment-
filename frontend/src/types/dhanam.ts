export type BranchCategory = 1 | 2 | 3; // 12AA | Mutuals | AOP
export type ReportType = 1 | 2 | 3 | 4 | 5 | 6 | 7; // Daily..HELP
export type GroupType = 1 | 2 | 3; // Regular | Defunct | All

export const BRANCH_CATEGORY_LABELS: Record<BranchCategory, string> = {
  1: "12AA",
  2: "Mutuals",
  3: "AOP",
};

export const REPORT_TYPE_LABELS: Record<ReportType, string> = {
  1: "Daily",
  2: "Weekly",
  3: "Monthly",
  4: "Annual",
  5: "CFM",
  6: "AREAP",
  7: "HELP",
};

export const GROUP_TYPE_LABELS: Record<GroupType, string> = {
  1: "Regular groups",
  2: "Defunct groups",
  3: "All groups",
};

/**
 * Request body for POST /api/dhanam/consolidation/generate. reportDate is dd-mm-yyyy.
 *
 * Note: backupDate, consolidationYear, finYear, finYearCode, finYearStartDate
 * are gone - this is now a read-only, single-schema report with no
 * dv_locreptandpayment/reptdt persistence.
 */
export interface ConsolidationRequest {
  reportDate: string;
  backupDate: string;
  month: number;
  branchCategory: BranchCategory;
  branchNo: string;
  reportType: ReportType;
  groupType: GroupType;
  /** Fiscal year to use for the "opening balance as of" cutoff date, e.g. "2026". */
  expectedAccountingYear: string;
  /** Display financial year, e.g. "2025-2026". Stored into reptdt/dv_locreptandpayment. */
  finYear: string;
  /** Financial year code, e.g. "2526". Stored into reptdt/dv_locreptandpayment. */
  finYearCode: string;
  /** Financial year start date, dd-mm-yyyy. Stored into reptdt/dv_locreptandpayment as fromdt. */
  finYearStartDate: string;
}

export interface ReportRow {
  accountCode: string;
  accountName: string;
  amounts: number[];
}

export interface ConsolidationResponse {
  message: string;
  columns: string[];
  receiptRows: ReportRow[];
  paymentRows: ReportRow[];
  receiptTotals: number[];
  paymentTotals: number[];
  regionName: string;
  districtName: string;
  stateName: string;
  reportLevel: string;
  levelName: string;
  groupByLabel: string;
  reportTypeLabel: string;
  reportDateLabel: string;
  backupDateLabel: string;
  reportForLabel: string;
  resultsRunId: number | null;
  resultsLinesStored: number;
}

export interface ApiError {
  error?: string;
  [fieldName: string]: string | undefined;
}
