import assert from "node:assert/strict";
import test from "node:test";
import { createRadarApiClient } from "../src/radarApiClient.js";

test("saveReport는 API 토큰과 보고서 본문을 Phase 1 API로 전달한다", async () => {
  let capturedUrl;
  let capturedOptions;
  const client = createRadarApiClient({
    baseUrl: "https://example.com/",
    apiToken: "secret",
    fetchImpl: async (url, options) => {
      capturedUrl = url;
      capturedOptions = options;
      return new Response(JSON.stringify({ id: 1 }), { status: 200 });
    }
  });

  const result = await client.saveReport({ reportDate: "2026-08-29" });

  assert.equal(capturedUrl, "https://example.com/api/v1/radar/reports");
  assert.equal(capturedOptions.method, "POST");
  assert.equal(capturedOptions.headers.get("X-API-Token"), "secret");
  assert.deepEqual(JSON.parse(capturedOptions.body), { reportDate: "2026-08-29" });
  assert.deepEqual(result, { id: 1 });
});

test("searchSignals는 값이 있는 필터만 쿼리 문자열로 전달한다", async () => {
  let capturedUrl;
  const client = createRadarApiClient({
    baseUrl: "https://example.com",
    apiToken: "secret",
    fetchImpl: async (url) => {
      capturedUrl = url;
      return new Response(JSON.stringify({ content: [] }), { status: 200 });
    }
  });

  await client.searchSignals({ company: "OpenAI", importance: "HIGH", page: 0, category: undefined });

  assert.equal(capturedUrl, "https://example.com/api/v1/radar/signals?company=OpenAI&importance=HIGH&page=0");
});

test("Radar API 오류는 호출자에게 안전한 오류 메시지로 전달한다", async () => {
  const client = createRadarApiClient({
    baseUrl: "https://example.com",
    apiToken: "secret",
    fetchImpl: async () => new Response(JSON.stringify({ message: "유효하지 않은 보고서입니다." }), { status: 400 })
  });

  await assert.rejects(() => client.getReport({ reportDate: "2026-08-29" }), /유효하지 않은 보고서입니다/);
});
