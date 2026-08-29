const defaultFetch = globalThis.fetch;

export function createRadarApiClient({ baseUrl, apiToken, fetchImpl = defaultFetch }) {
  if (!baseUrl) {
    throw new Error("MCP_API_BASE_URL 환경변수가 필요합니다.");
  }
  if (!apiToken) {
    throw new Error("MCP_API_TOKEN 환경변수가 필요합니다.");
  }
  if (!fetchImpl) {
    throw new Error("Fetch API를 사용할 수 없습니다.");
  }

  const normalizedBaseUrl = baseUrl.replace(/\/$/, "");

  async function request(path, options = {}) {
    const headers = new Headers(options.headers);
    headers.set("Accept", "application/json");
    headers.set("X-API-Token", apiToken);
    if (options.body) headers.set("Content-Type", "application/json");

    const response = await fetchImpl(`${normalizedBaseUrl}${path}`, { ...options, headers });
    const body = await parseResponse(response);
    if (!response.ok) {
      const message = typeof body?.message === "string" ? body.message : `Radar API 요청 실패 (${response.status})`;
      throw new Error(message);
    }
    return body;
  }

  return {
    saveReport: (report) => request("/api/v1/radar/reports", {
      method: "POST",
      body: JSON.stringify(report)
    }),
    getReport: ({ reportDate, reportType = "AI_SERVICES_RADAR" }) => request(
      `/api/v1/radar/reports/${encodeURIComponent(reportDate)}?${new URLSearchParams({ reportType })}`
    ),
    searchSignals: (filters) => {
      const query = new URLSearchParams();
      for (const [key, value] of Object.entries(filters)) {
        if (value !== undefined && value !== null && value !== "") query.set(key, String(value));
      }
      const suffix = query.size > 0 ? `?${query}` : "";
      return request(`/api/v1/radar/signals${suffix}`);
    }
  };
}

async function parseResponse(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}
