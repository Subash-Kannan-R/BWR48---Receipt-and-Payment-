import { useState, FormEvent } from "react";
import type {
  ConsolidationRequest,
  BranchCategory,
  ReportType,
  GroupType,
} from "../types/dhanam";
import {
  BRANCH_CATEGORY_LABELS,
  REPORT_TYPE_LABELS,
  GROUP_TYPE_LABELS,
} from "../types/dhanam";

const MONTHS = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

/** yyyy-mm-dd (native <input type="date"> value) -> dd-mm-yyyy (backend format). */
function toDdMmYyyy(isoDate: string): string {
  if (!isoDate) return "";
  const [y, m, d] = isoDate.split("-");
  return `${d}-${m}-${y}`;
}

interface Props {
  onSubmit: (request: ConsolidationRequest) => void;
  submitting: boolean;
}

export default function DhanamConsolidationForm({ onSubmit, submitting }: Props) {
  // UI-only for now — not part of ConsolidationRequest yet. See note at bottom of file.
  const [reportCategory, setReportCategory] = useState<"baseline" | "receiptPayment">("receiptPayment");

  const [reportDate, setReportDate] = useState("");
  const [backupDate, setBackupDate] = useState("");
  const [month, setMonth] = useState(1);
  const [branchCategory, setBranchCategory] = useState<BranchCategory>(1);
  const [branchNo, setBranchNo] = useState("");
  const [reportType, setReportType] = useState<ReportType>(3);
  const [groupType, setGroupType] = useState<GroupType>(3);
  const [expectedAccountingYear, setExpectedAccountingYear] = useState("2026");
  const [finYear, setFinYear] = useState("2025-2026");
  const [finYearCode, setFinYearCode] = useState("2526");
  const [finYearStartDate, setFinYearStartDate] = useState("");

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    onSubmit({
      reportDate: toDdMmYyyy(reportDate),
      backupDate: toDdMmYyyy(backupDate),
      month,
      branchCategory,
      branchNo,
      reportType,
      groupType,
      expectedAccountingYear,
      finYear,
      finYearCode,
     finYearStartDate: toDdMmYyyy(finYearStartDate),    });
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="relative overflow-hidden rounded-2xl border border-white/30 bg-white/70 backdrop-blur-xl shadow-[0_8px_32px_rgba(0,0,0,0.04)] transition-all hover:shadow-[0_12px_40px_rgba(0,0,0,0.06)]"
    >
      {/* Decorative top gradient bar */}
      <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-indigo-500 via-teal-500 to-emerald-500" />

      <div className="px-6 py-5 sm:px-8">
        {/* Form header */}
        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-100 text-indigo-600">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <div>
            <h2 className="text-lg font-bold text-slate-800">Report Parameters</h2>
            <p className="text-xs text-slate-500">Fill in the fields below to generate the consolidation.</p>
          </div>
        </div>

        {/* Section 1: Report parameters */}
        <div className="space-y-4">
          {/* Consolidation Type toggle */}
          <div className="mb-2">
            <span className="mb-1.5 block text-xs font-medium text-slate-600">Consolidation Type</span>
            <div className="flex gap-3">
              <label
                className={`flex-1 cursor-pointer rounded-xl border px-4 py-2.5 text-center text-sm font-medium transition-all ${
                  reportCategory === "baseline"
                    ? "border-indigo-400 bg-indigo-50 text-indigo-700 shadow-sm"
                    : "border-slate-200/80 bg-white/90 text-slate-600 hover:border-slate-300"
                }`}
              >
                <input
                  type="radio"
                  name="reportCategory"
                  value="baseline"
                  checked={reportCategory === "baseline"}
                  onChange={() => setReportCategory("baseline")}
                  className="sr-only"
                />
                Consolidation Baseline
              </label>
              <label
                className={`flex-1 cursor-pointer rounded-xl border px-4 py-2.5 text-center text-sm font-medium transition-all ${
                  reportCategory === "receiptPayment"
                    ? "border-indigo-400 bg-indigo-50 text-indigo-700 shadow-sm"
                    : "border-slate-200/80 bg-white/90 text-slate-600 hover:border-slate-300"
                }`}
              >
                <input
                  type="radio"
                  name="reportCategory"
                  value="receiptPayment"
                  checked={reportCategory === "receiptPayment"}
                  onChange={() => setReportCategory("receiptPayment")}
                  className="sr-only"
                />
                BWR48 - Receipt and Payment
              </label>
            </div>
          </div>

          <SectionHeader icon="calendar" title="Time & Date" />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Field label="Report date">
              <input type="date" required value={reportDate} onChange={(e) => setReportDate(e.target.value)} className={inputClass} />
            </Field>
            <Field label="Backup date">
              <input type="date" required value={backupDate} onChange={(e) => setBackupDate(e.target.value)} className={inputClass} />
            </Field>
            <Field label="Accounting month">
              <select value={month} onChange={(e) => setMonth(Number(e.target.value))} className={inputClass}>
                {MONTHS.map((name, i) => (
                  <option key={name} value={i + 1}>{name}</option>
                ))}
              </select>
            </Field>
            <Field label="Expected accounting year" hint="e.g. 2026">
              <input type="text" required value={expectedAccountingYear} onChange={(e) => setExpectedAccountingYear(e.target.value)} className={inputClass} />
            </Field>
          </div>

          <div className="mt-6">
            <SectionHeader icon="office" title="Branch & Report" />
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <Field label="Branch category">
                <select value={branchCategory} onChange={(e) => setBranchCategory(Number(e.target.value) as BranchCategory)} className={inputClass}>
                  {Object.entries(BRANCH_CATEGORY_LABELS).map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
              </Field>
              <Field label="Branch no">
                <input type="text" required value={branchNo} onChange={(e) => setBranchNo(e.target.value)} placeholder="e.g. 12" className={inputClass} />
              </Field>
              <Field label="Report type">
                <select value={reportType} onChange={(e) => setReportType(Number(e.target.value) as ReportType)} className={inputClass}>
                  {Object.entries(REPORT_TYPE_LABELS).map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
              </Field>
              <Field label="Group type">
                <select value={groupType} onChange={(e) => setGroupType(Number(e.target.value) as GroupType)} className={inputClass}>
                  {Object.entries(GROUP_TYPE_LABELS).map(([val, label]) => (
                    <option key={val} value={val}>{label}</option>
                  ))}
                </select>
              </Field>
            </div>
          </div>

          <div className="mt-6">
            <SectionHeader icon="calendar" title="Financial Year" />
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Field label="Financial year" hint="e.g. 2025-2026">
                <input type="text" required value={finYear} onChange={(e) => setFinYear(e.target.value)} className={inputClass} />
              </Field>
              <Field label="Financial year code" hint="e.g. 2526">
                <input type="text" required value={finYearCode} onChange={(e) => setFinYearCode(e.target.value)} className={inputClass} />
              </Field>
              <Field label="Financial year start date">
                <input type="date" required value={finYearStartDate} onChange={(e) => setFinYearStartDate(e.target.value)} className={inputClass} />
              </Field>
            </div>
          </div>
        </div>

        {/* Submit button */}
        <div className="mt-8 flex justify-end">
          <button
            type="submit"
            disabled={submitting}
            className="group relative inline-flex items-center gap-2 overflow-hidden rounded-xl bg-gradient-to-r from-indigo-600 to-teal-600 px-6 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/25 transition-all duration-300 hover:shadow-xl hover:shadow-indigo-500/30 hover:scale-[1.02] active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
          >
            {submitting ? (
              <>
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Generating…
              </>
            ) : (
              <>
                <svg className="h-4 w-4 transition-transform group-hover:rotate-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                Generate Consolidation
              </>
            )}
          </button>
        </div>
      </div>
    </form>
  );
}

/* --- Reusable field & section components --- */
const inputClass =
  "block w-full rounded-xl border border-slate-200/80 bg-white/90 px-4 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 shadow-sm transition-all duration-200 focus:border-indigo-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 hover:border-slate-300 disabled:bg-slate-50 disabled:text-slate-500";

function Field({ label, hint, children }: { label: string; hint?: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-xs font-medium text-slate-600">{label}</span>
      {children}
      {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
    </label>
  );
}

function SectionHeader({ icon, title }: { icon: "calendar" | "office"; title: string }) {
  const icons = {
    calendar: (
      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    office: (
      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
          d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
      </svg>
    ),
  };
  return (
    <div className="flex items-center gap-2 text-indigo-600 mb-1">
      {icons[icon]}
      <span className="text-xs font-semibold uppercase tracking-wide">{title}</span>
    </div>
  );
}

/*
 * NOTE: `reportCategory` ("Consolidation Baseline" vs "Receipt and Payment")
 * is still UI-only state - it isn't sent to the backend because
 * ConsolidationRequest has no matching field. Tell me what each option
 * should change and I'll wire it through the type, this payload, and
 * ConsolidationService.
 */
