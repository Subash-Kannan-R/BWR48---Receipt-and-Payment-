import { useState } from "react";
import DhanamConsolidationForm from "../components/DhanamConsolidationForm";
import DhanamConsolidationTable from "../components/DhanamConsolidationTable";
import { generateConsolidation, DhanamApiError } from "../api/dhanamApi";
import type { ConsolidationRequest, ConsolidationResponse } from "../types/dhanam";

export default function DhanamConsolidationPage() {
  const [result, setResult] = useState<ConsolidationResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(request: ConsolidationRequest) {
    setSubmitting(true);
    setError(null);
    setResult(null);
    try {
      const response = await generateConsolidation(request);
      setResult(response);
    } catch (err) {
      if (err instanceof DhanamApiError) {
        const fieldErrors = Object.entries(err.details)
          .filter(([k]) => k !== "error")
          .map(([k, v]) => `${k}: ${v}`)
          .join(", ");
        setError(err.details.error ?? fieldErrors ?? "Request failed");
      } else {
        setError("Could not reach the server. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
      <header className="mb-6">
        <h1 className="text-xl font-semibold text-slate-900">
          Consolidation of Dhanam Data — Receipts &amp; Payments
        </h1>
        <p className="mt-1 text-sm text-slate-500">
          Receipts and payments for the selected branch, computed live from federation, accthead,
          grpbalsheet, grpdetail, rcpttran and vouchtran.
        </p>
      </header>

      <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <DhanamConsolidationForm onSubmit={handleSubmit} submitting={submitting} />
      </div>

      {error && (
        <div className="mt-6 rounded-md border border-red-300 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {result && (
        <div className="mt-6">
          <DhanamConsolidationTable data={result} />
        </div>
      )}
    </div>
  );
}

