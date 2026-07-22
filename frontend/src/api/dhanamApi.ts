import type { ConsolidationRequest, ConsolidationResponse, ApiError } from "../types/dhanam";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8082";

export class DhanamApiError extends Error {
  status: number;
  details: ApiError;
  constructor(status: number, details: ApiError) {
    super(details.error ?? "Request failed");
    this.status = status;
    this.details = details;
  }
}

/** Calls POST /api/dhanam/consolidation/generate. Read-only - no persistence, no "already generated" state. */
export async function generateConsolidation(
  request: ConsolidationRequest
): Promise<ConsolidationResponse> {
  const res = await fetch(`${API_BASE}/api/dhanam/consolidation/generate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });

  const body = await res.json();

  if (!res.ok) {
    throw new DhanamApiError(res.status, body as ApiError);
  }
  return body as ConsolidationResponse;
}
