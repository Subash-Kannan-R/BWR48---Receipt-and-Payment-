import type { ConsolidationResponse } from "../types/dhanam";

function formatAmount(n: number): string {
  if (!n) return "0";
  return n.toLocaleString("en-IN");
}

interface SectionProps {
  title: string;
  columns: string[];
  rows: ConsolidationResponse["receiptRows"];
  totals: number[];
}

/** Row model: account heads run down the left as rows, one column per location. */
function Section({ title, columns, rows, totals }: SectionProps) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="bg-gradient-to-r from-sky-500 to-sky-400 text-white">
            <th className="px-3 py-2 text-left font-semibold w-20">S.L. Code</th>
            <th className="px-3 py-2 text-left font-semibold">Particulars</th>
            {columns.map((col) => (
              <th key={col} className="px-3 py-2 text-right font-semibold whitespace-nowrap">
                {col}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 bg-white">
          {rows.length === 0 ? (
            <tr>
              <td colSpan={2 + columns.length} className="px-3 py-4 text-center text-slate-400">
                No entries
              </td>
            </tr>
          ) : (
            rows.map((row, idx) => (
              <tr key={row.accountCode} className={idx % 2 === 1 ? "bg-slate-50/60" : ""}>
                <td className="px-3 py-1.5 text-slate-500">{row.accountCode}</td>
                <td className="px-3 py-1.5 text-slate-800">{row.accountName}</td>
                {row.amounts.map((amt, i) => (
                  <td key={i} className="px-3 py-1.5 text-right tabular-nums text-slate-700">
                    {formatAmount(amt)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
        <tfoot className="bg-slate-100 font-semibold">
          <tr>
            <td colSpan={2} className="px-3 py-2 text-slate-800">Total</td>
            {totals.map((t, i) => (
              <td key={i} className="px-3 py-2 text-right tabular-nums text-slate-900">
                {formatAmount(t)}
              </td>
            ))}
          </tr>
        </tfoot>
      </table>
      <div className="border-t border-slate-200 bg-slate-50 px-3 py-1.5 text-right text-xs font-medium text-slate-500">
        {title}
      </div>
    </div>
  );
}

function MetaField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span className="text-xs font-medium text-slate-500">{label} : </span>
      <span className="text-xs font-semibold text-slate-800">{value || "—"}</span>
    </div>
  );
}

export default function DhanamConsolidationTable({ data }: { data: ConsolidationResponse }) {
  return (
    <div className="space-y-4">
      {/* BWR48-style banner */}
      <div className="overflow-hidden rounded-lg border border-sky-200">
        <div className="bg-gradient-to-r from-sky-600 to-sky-500 px-5 py-3">
          <h2 className="text-center text-base font-semibold text-white">
            BWR48 - Consolidation of {data.reportForLabel} Data - Status of Location Wise Receipts &amp; Payments
          </h2>
        </div>
        <div className="grid grid-cols-1 gap-x-6 gap-y-2 bg-white px-5 py-3 sm:grid-cols-2 lg:grid-cols-4">
          <MetaField label="Report Level" value={data.reportLevel} />
          <MetaField label="Level Name" value={data.levelName} />
          <MetaField label="Group By" value={data.groupByLabel} />
          <MetaField label="Report For" value={data.reportForLabel} />
          <MetaField label="Report Type" value={data.reportTypeLabel} />
          <MetaField label="Report Date" value={data.reportDateLabel} />
          <MetaField label="Backup Date" value={data.backupDateLabel} />
        </div>
      </div>

      <div className="flex items-center justify-between text-sm">
        <span className="text-slate-600">{data.message}</span>
        {data.resultsRunId != null ? (
          <span className="text-emerald-600">
            {/* Saved to result.reptdt (recordno {data.resultsRunId}) + {data.resultsLinesStored} rows in result.dv_locreptandpayment */}
          </span>
        ) : (
          <span className="text-amber-600">Not saved to the result database — check backend logs</span>
        )}
      </div>

      <Section title="Receipts" columns={data.columns} rows={data.receiptRows} totals={data.receiptTotals} />
      <Section title="Payments" columns={data.columns} rows={data.paymentRows} totals={data.paymentTotals} />
    </div>
  );
}


