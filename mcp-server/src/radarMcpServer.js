import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";

const sourceSchema = z.object({
  publisher: z.string().min(1).max(200),
  title: z.string().min(1).max(500),
  url: z.string().url().max(2000),
  publishedDate: z.string().date().optional(),
  sourceType: z.string().min(1).max(80)
});

const signalSchema = z.object({
  company: z.string().min(1).max(160),
  category: z.string().min(1).max(80),
  importance: z.string().min(1).max(40),
  signal: z.string().min(1),
  fact: z.string().min(1),
  whatChanged: z.string().min(1),
  industryImpact: z.string().min(1),
  opportunity: z.string().optional(),
  threat: z.string().optional(),
  structuralRisk: z.string().optional(),
  practicalImplication: z.string().optional(),
  recommendedAction: z.string().min(1),
  sources: z.array(sourceSchema).min(1)
});

const reportSchema = {
  reportDate: z.string().date(),
  reportType: z.string().min(1).max(80),
  title: z.string().min(1).max(300),
  executiveView: z.string().min(1),
  strategicInterpretation: z.string().min(1),
  markdown: z.string().min(1),
  promptVersion: z.string().max(100).optional(),
  signals: z.array(signalSchema).min(1)
};

function textResult(data) {
  return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
}

function errorResult(error) {
  return { content: [{ type: "text", text: error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다." }], isError: true };
}

export function createRadarMcpServer(radarApiClient) {
  const server = new McpServer({ name: "ai-msp-radar", version: "1.0.0" });

  server.tool(
    "save_radar_report",
    "AI Services Radar 보고서 전체를 저장하거나 같은 보고서 유형·날짜의 기존 보고서를 갱신합니다.",
    reportSchema,
    async (report) => {
      try {
        return textResult(await radarApiClient.saveReport(report));
      } catch (error) {
        return errorResult(error);
      }
    }
  );

  server.tool(
    "get_radar_report",
    "보고서 날짜와 유형으로 저장된 AI Services Radar 보고서 전체를 조회합니다.",
    {
      reportDate: z.string().date().describe("조회할 보고서 날짜 (YYYY-MM-DD)"),
      reportType: z.string().min(1).max(80).default("AI_SERVICES_RADAR")
    },
    async ({ reportDate, reportType }) => {
      try {
        return textResult(await radarApiClient.getReport({ reportDate, reportType }));
      } catch (error) {
        return errorResult(error);
      }
    }
  );

  server.tool(
    "search_radar_signals",
    "기간, 보고서 유형, 기업, 카테고리, 중요도와 검색어로 저장된 Radar 신호를 조회합니다.",
    {
      reportType: z.string().max(80).optional(),
      fromDate: z.string().date().optional(),
      toDate: z.string().date().optional(),
      company: z.string().max(160).optional(),
      category: z.string().max(80).optional(),
      importance: z.string().max(40).optional(),
      q: z.string().max(300).optional(),
      page: z.number().int().min(0).default(0),
      size: z.number().int().min(1).max(50).default(20)
    },
    async (filters) => {
      try {
        return textResult(await radarApiClient.searchSignals(filters));
      } catch (error) {
        return errorResult(error);
      }
    }
  );

  return server;
}
