import crypto from "node:crypto";
import express from "express";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { createRadarApiClient } from "./radarApiClient.js";
import { createRadarMcpServer } from "./radarMcpServer.js";

const port = Number(process.env.PORT ?? 3001);
const mcpServerToken = process.env.MCP_SERVER_TOKEN;
if (!mcpServerToken) {
  throw new Error("MCP_SERVER_TOKEN 환경변수가 필요합니다.");
}
const radarApiClient = createRadarApiClient({
  baseUrl: process.env.MCP_API_BASE_URL,
  apiToken: process.env.MCP_API_TOKEN
});
const transports = new Map();
const app = express();

app.use(express.json({ limit: "2mb" }));
app.use("/mcp", (request, response, next) => {
  const authorization = request.get("Authorization");
  if (authorization === `Bearer ${mcpServerToken}`) return next();
  response.status(401).json({ error: "인증이 필요합니다." });
});

app.post("/mcp", async (request, response) => {
  const sessionId = request.get("mcp-session-id");
  let transport = sessionId ? transports.get(sessionId) : undefined;
  if (!transport) {
    transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: () => crypto.randomUUID(),
      onsessioninitialized: (newSessionId) => transports.set(newSessionId, transport)
    });
    transport.onclose = () => {
      if (transport.sessionId) transports.delete(transport.sessionId);
    };
    await createRadarMcpServer(radarApiClient).connect(transport);
  }
  await transport.handleRequest(request, response, request.body);
});

app.get("/mcp", async (request, response) => {
  const transport = transports.get(request.get("mcp-session-id"));
  if (!transport) return response.status(404).json({ error: "MCP 세션을 찾을 수 없습니다." });
  await transport.handleRequest(request, response);
});

app.delete("/mcp", async (request, response) => {
  const transport = transports.get(request.get("mcp-session-id"));
  if (!transport) return response.status(404).json({ error: "MCP 세션을 찾을 수 없습니다." });
  await transport.handleRequest(request, response);
});

app.get("/health", (_request, response) => response.json({ status: "UP" }));

app.listen(port, () => {
  console.log(`AI MSP Radar MCP 서버가 ${port} 포트에서 실행 중입니다.`);
});
