# AI Services Radar Phase 2: MCP 어댑터

## 목적

`mcp-server`는 AI Services Radar 작업이 Phase 1 API를 도구로 호출할 수 있게 하는 별도 MCP 서버다. 데이터베이스에 직접 연결하거나 도메인 로직을 중복 구현하지 않는다.

```text
MCP Client
  └─ Streamable HTTP /mcp
       └─ MCP Adapter
            └─ AI MSP Intelligence REST API
                 └─ Service / Transaction / PostgreSQL
```

OpenAI의 원격 MCP는 Streamable HTTP 또는 HTTP/SSE 전송을 지원해야 한다. 이 구현은 Streamable HTTP를 사용한다. [OpenAI MCP 및 Connectors 공식 문서](https://developers.openai.com/api/docs/guides/tools-connectors-mcp)를 참고한다.

## 제공 도구

| 도구 | 역할 |
|---|---|
| `save_radar_report` | 보고서 전체를 저장하거나 같은 `reportType + reportDate`의 보고서를 업서트한다. |
| `get_radar_report` | 날짜와 보고서 유형으로 전체 보고서를 조회한다. |
| `search_radar_signals` | 기간, 기업, 카테고리, 중요도, 검색어로 신호를 조회한다. |

## 환경 변수

| 변수 | 설명 |
|---|---|
| `MCP_API_BASE_URL` | Phase 1 백엔드 주소. 운영에서는 `https://aimsp-backend.onrender.com`이다. |
| `MCP_API_TOKEN` | 백엔드의 `API_SECRET_TOKEN`과 같은 값. MCP 어댑터가 REST API 호출 시 `X-API-Token`으로 전달한다. |
| `MCP_SERVER_TOKEN` | MCP 공개 엔드포인트를 보호하는 별도 비밀값. 클라이언트는 `Authorization: Bearer <값>`을 보낸다. |
| `PORT` | MCP 서버 포트. 기본값은 `3001`이다. |

`MCP_SERVER_TOKEN`은 필수값이다. 누락하면 MCP 서버가 시작되지 않으므로, 인증이 비활성화된 상태로 배포되지 않는다. 로컬 설정 예시는 [`mcp-server/.env.example`](../mcp-server/.env.example)에 있다.

## 배포 및 연결

1. `main` 브랜치 배포 후 Render에서 `aimsp-radar-mcp` 서비스를 생성하거나 `render.yaml` 변경을 반영한다.
2. Render 환경 변수에 `MCP_API_TOKEN`과 `MCP_SERVER_TOKEN`을 비밀값으로 등록한다.
3. MCP 클라이언트의 서버 URL을 `https://aimsp-radar-mcp.onrender.com/mcp`로 등록한다.
4. 클라이언트가 Bearer 토큰 설정을 지원하면 `MCP_SERVER_TOKEN`을 전달한다. OAuth 기반 Custom App 연결이 필요하면 이 서버 앞단에 OAuth 인증 계층을 추가한 뒤 해당 URL을 등록한다.

쓰기 도구인 `save_radar_report`는 MCP 클라이언트 측에서도 항상 사용자 승인을 요구하도록 구성하는 것을 권장한다.

## 검증

```bash
cd mcp-server
npm ci
npm test
```
