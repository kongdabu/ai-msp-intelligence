# AI MSP Intelligence Platform

## 목적

한국 금융·공공 엔터프라이즈 시장을 대상으로 하는 AI 전략팀의 AI 생태계·서비스 사업모델 모니터링 플랫폼이다. Frontier AI Labs, CSP·플랫폼 사업자, 글로벌/국내 컨설팅펌 및 SI·MSP 동향, FDE·RDE·ODE 딜리버리 모델, AI 과금 체계(AI Pricing), Agentic AI·AIOps 동향을 자동 수집하고 Gemini로 분석한다.

## 기술 구성

- Backend: Java 21, Spring Boot 3.3.5, Gradle, JPA/Hibernate, Flyway
- Database: 로컬 H2 / 운영 Supabase PostgreSQL
- AI: Google Gemini API (`gemini-3.5-flash-lite`), OkHttp
- Frontend: React 18, TypeScript strict mode, Vite, Tailwind CSS, TanStack Query, Zustand
- 배포: Render(백엔드 Docker), Vercel(프런트엔드)

## 주요 구조

```
backend/src/main/java/com/aimsp/intelligence/
├── ai/           # Gemini 클라이언트(Rate Limit/쿨다운), 작업 코디네이터, 요약·인사이트·배틀카드·트렌드 생성기
├── crawler/      # Naver 뉴스 API 및 공식 사이트 크롤러, 수집 오케스트레이터, 백그라운드 작업 관리
├── config/       # Gemini, CORS, API 토큰, 스케줄 설정, 로깅
├── domain/       # article, radar, insight, battlecard, trend, source, config
├── dto/          # API 요청/응답 DTO (RadarDto, TrendNewsDto 등 포함)
└── exception/    # 전역 예외 처리 (AiApiUnavailableException 등)

frontend/src/
├── components/   # radar, trend, article, insight, battlecard, dashboard, layout, common
├── hooks/        # React Query 기반 API 조회 및 뮤테이션 훅
├── pages/        # Radar, WatchListSettings, Articles, TrendNews, Insights, Battlecards, Sources, Settings
├── store/        # Zustand 상태 관리
└── types/        # TypeScript 공통 타입
```

## 핵심 도메인 및 동작

- **AI Services Industry Radar**: 35개 글로벌/국내 주요 사업자(Watch List)를 대상으로 6개 핵심 산업 재편 관점(`AI_AGENT`, `FRONTIER_LABS`, `PARTNERSHIP`, `DEPLOYMENT_MODEL`, `AI_PRICING`, `AGENTIC_OPERATIONS`)의 검증된 신호(`RadarSignal`)를 포착·분석하고 주간 브리핑(`RadarWeeklyBrief`)을 제공한다.
- **전략 보고서 (Strategic Report)**: 축적된 Radar 신호를 종합하여 Consulting–SI–MSP–Application ITO 밸류체인 재편, FDE 딜리버리, AI Pricing 전이, Agentic ITO 심층 분석 및 국내 MSP 관점 Top 3 Action을 자동 도출한다.
- **기사 수집 및 AI 요약**: 공식 사이트(Frontier Labs, 글로벌 컨설팅 등) 및 Naver 뉴스로부터 신규 기사를 수집하고 Gemini로 핵심 사실 요약, 관련도 점수, 카테고리를 분류한다.
- **전략 인사이트**: 축적된 원문 기사를 바탕으로 기회/위협/전략 인사이트를 도출하고 근거 기사를 연결한다.
- **경쟁사 배틀카드**: 주요 경쟁사(LG CNS, SK AX, 베스핀글로벌, 삼일PwC 등)의 최근 동향을 기반으로 SWOT 분석 및 MSP 대응 전략을 도출한다.
- **트렌드 뉴스**: 시장 전체를 관통하는 핵심 트렌드 뉴스와 AI 분석 결과를 제공한다.
- **출처 유효성 검증**: 등록된 Radar 신호의 원문 URL을 주기적으로 검증하여 유효하지 않은 원문(`404`/`410`)을 화면 및 브리핑에서 자동 제외한다.

## 자동 스케줄 (Asia/Seoul)

- `0 0 1 * * *` (매일 01:00): 정기 원문 기사 수집 (`CrawlerOrchestrator.crawlAll()`)
- `0 0 2 * * *` (매일 02:00): 전략 인사이트 생성 (`InsightService.generateInsights()`)
- `0 30 1,7,13,19 * * *` (하루 4회): 보류 기사 AI 재분석 및 Radar Signal 심층 처리 (`ArticleAnalysisRetryService`, `RadarCollectionService`)
- `0 0 7 * * MON,WED` (매주 월·수 07:00): 전략 보고서 자동 생성 (`StrategyReportService.generateReport()`)
- 매 1시간 주기: 최근 Radar Signal 원문 링크 접근성 재확인 (`RadarSourceVerificationService`)

## Gemini 설정

- 기본 모델: `gemini-3.5-flash-lite` (환경변수 `GEMINI_MODEL`로 변경 가능)
- API 형식: `v1beta/models/{model}:generateContent`
- 응답 형식: `application/json` (`responseMimeType`) 강제
- Rate Limit 및 재시도: `GEMINI_RATE_LIMIT_MS`(기본 10초) 간격 제어, 429/503 오류 시 최대 3회 지수 백오프 재시도 및 공용 쿨다운 적용
- 단일 실행 보장: `GeminiWorkCoordinator`를 통해 AI 분석 작업 간 상호 배제 및 리소스 보호

## 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | Gemini API 키 | 필수 |
| `GEMINI_MODEL` | Gemini 모델 ID | `gemini-3.5-flash-lite` |
| `GEMINI_RATE_LIMIT_MS` | Gemini API 최소 호출 간격(밀리초) | `10000` |
| `CRAWL_NAVER_RESULT_LIMIT` | 검색어당 Naver 뉴스 최대 수집 건수 | `10` |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | Naver 검색 API 인증 정보 | 선택 |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 운영 PostgreSQL 연결 정보 | 운영 필수 |
| `SPRING_PROFILES_ACTIVE` | `prod`이면 PostgreSQL 설정 사용 | 기본 프로필(H2) |
| `CORS_ALLOWED_ORIGINS` | 허용할 CORS 오리진 목록 | 로컬·Vercel 오리진 |
| `API_SECRET_TOKEN` | 변경 API 보호 토큰 (`X-API-Token` 헤더 검증) | 미설정 시 비활성 |

## 개발 및 배포 규칙

- Java 21과 Lombok을 사용한다.
- TypeScript는 strict mode를 유지하고 `any`를 사용하지 않는다.
- React는 함수형 컴포넌트로 작성하고, 공통 UI는 재사용 가능한 컴포넌트로 분리한다.
- 화면은 반응형으로 구현한다.
- API 응답 형식은 일관되게 유지하고, 예외는 `GlobalExceptionHandler`에서 처리한다.
- `main` 브랜치 푸시는 Render와 Vercel의 자동 배포를 트리거한다.
- Render 헬스체크 엔드포인트: `/actuator/health`
- Vercel은 `frontend/vercel.json`으로 `/api/*` 요청을 Render 백엔드로 프록시한다.

