# AI MSP Intelligence Platform

## 목적
AI MSP 사업 전략팀용 글로벌 AI 생태계 및 경쟁사 동향 모니터링 플랫폼.
Frontier AI Labs, CSP·플랫폼, 글로벌/국내 컨설팅펌 및 SI·MSP(LG CNS, SK AX, 삼성SDS, 베스핀글로벌, 삼일PwC 등)의 AI/AI Agent/과금 체계/딜리버리 모델/ITO 관련 뉴스와 공식 발표를 자동 수집·요약·분석하여 전략적 인사이트와 AI Services Industry Radar를 제공한다.

## 비즈니스 컨텍스트
- **타겟**: 한국 금융·공공 엔터프라이즈 시장
- **서비스**: AI MSP (구독 + 성과 기반 혼합, FinOps for AI)
- **핵심 차별화**: AI Agent 기반 ITO 전환(Agentic ITO), 현장 투입형 딜리버리(FDE·RDE·ODE), Vertical Agent IP
- **웹 애플리케이션**: https://ai-msp-intelligence.vercel.app
- **백엔드 API**: https://aimsp-backend.onrender.com

---

## 기술 스택

### Backend
- **Core**: Java 21, Spring Boot 3.3.5, Gradle
- **ORM/DB**: JPA/Hibernate, Flyway, Lombok, H2(로컬) / Supabase PostgreSQL(운영)
- **HTTP/Crawler**: OkHttp3 4.12.0 (Gemini API 및 원문 수집), Jsoup 1.17.2 (HTML 파싱/정제)
- **AI**: Google Gemini API (`gemini-3.5-flash-lite`)
  - **Rate Limit**: 10초 간격 강제 제어 (`GEMINI_RATE_LIMIT_MS`)
  - **Retry**: 최대 3회 재시도, 429/503 오류 시 지수 백오프 및 retryDelay 헤더 파싱 대기
  - **Concurrency**: `GeminiWorkCoordinator`를 통한 단일 실행 제어 및 상호 배제
  - **Format**: `application/json` 응답 강제 (`responseMimeType`)
  - **Timeout**: connect 30s / read 120s / write 30s

### Frontend
- **Framework**: React 18.2.0, TypeScript 5.2.2 (strict mode), Vite 5.2.0
- **Styling**: Tailwind CSS 3.4.3, Radix UI, Lucide React
- **State/Query**: TanStack Query 5.28.6 (staleTime: 5분), Zustand 4.5.2
- **Routing/HTTP**: React Router 6.22.3, Axios 1.6.8
- **Visualization**: Recharts 2.12.3

---

## 프로젝트 구조

```
ai-msp-intelligence/
├── backend/                          # Spring Boot API 서버
│   ├── src/main/java/com/aimsp/intelligence/
│   │   ├── AiMspApplication.java
│   │   ├── ai/
│   │   │   ├── GeminiApiClient.java        # Gemini API 클라이언트 (Rate Limit, Retry, 쿨다운)
│   │   │   ├── GeminiWorkCoordinator.java  # Gemini 작업 상호 배제 코디네이터
│   │   │   ├── SummaryGenerator.java       # 기사 3줄 요약·카테고리·관련도 점수 생성
│   │   │   ├── InsightGenerator.java       # 전략 인사이트 생성
│   │   │   ├── InsightValidator.java       # 인사이트 수치 및 비즈니스 검증
│   │   │   ├── BattleCardGenerator.java    # 경쟁사 SWOT + MSP 대응 전략 생성
│   │   │   └── TrendNewsGenerator.java     # 트렌드 뉴스 AI 분석 생성
│   │   ├── crawler/
│   │   │   ├── CrawlerOrchestrator.java    # 전체 크롤링 조율 (공식 사이트 + Naver 뉴스)
│   │   │   ├── CrawlJobService.java        # 크롤링 백그라운드 작업 관리 (실행/취소/상태)
│   │   │   ├── NaverNewsClient.java        # Naver 뉴스 검색 API 클라이언트
│   │   │   ├── OfficialSiteCrawler.java    # 공식 사이트/블로그/피드/사이트맵 크롤러
│   │   │   ├── ContentSourceCrawler.java   # 크롤러 인터페이스
│   │   │   └── sources/
│   │   │       └── AiEcosystemCrawler.java # AI 생태계·과금·인재 키워드 기반 수집
│   │   ├── domain/
│   │   │   ├── article/                    # Article 엔티티, CRUD, 보류 기사 재분석
│   │   │   ├── radar/                      # RadarPlayer, Signal, Assessment, WeeklyBrief, 수집/검증 서비스
│   │   │   ├── insight/                    # Insight 엔티티, 연관 기사 매핑, 북마크
│   │   │   ├── battlecard/                 # BattleCard 엔티티, SWOT 분석
│   │   │   ├── trend/                      # TrendNews 엔티티 및 트렌드 분석
│   │   │   ├── source/                     # Source 수집처 관리
│   │   │   └── config/                     # SystemConfig 설정 엔티티
│   │   ├── config/
│   │   │   ├── AppConfig.java              # 환경 변수 바인딩
│   │   │   ├── ApiTokenFilter.java         # X-API-Token 헤더 기반 API 보안 필터
│   │   │   ├── CorsConfig.java             # CORS 허용 설정
│   │   │   ├── SchedulerConfig.java        # 정기 배치 스케줄러
│   │   │   └── TaskExecutionLogger.java    # 배치 작업 로깅
│   │   ├── dto/                            # API 요청/응답 DTO (RadarDto, TrendNewsDto 등)
│   │   └── exception/                      # GlobalExceptionHandler, AiApiUnavailableException
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/                   # Flyway 스키마 마이그레이션 (V1 ~ V17)
│   └── Dockerfile
│
├── frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/                          # Radar, WatchListSettings, Articles, TrendNews, Insights, BattleCards, Sources, Settings, Dashboard
│   │   ├── components/                     # radar/, trend/, article/, insight/, battlecard/, dashboard/, layout/, common/
│   │   ├── hooks/                          # useRadar, useArticles, useInsights, useBattlecards, useTrendNews, useDashboard, useSystemConfig
│   │   ├── store/                          # Zustand 스토어
│   │   └── types/                          # TypeScript 공통 타입 인터페이스
│   ├── vite.config.ts
│   └── vercel.json                         # /api/* → Render 백엔드 리버스 프록시
│
├── render.yaml
└── docs/
    └── ai-services-industry-radar.md       # AI Services Industry Radar 사양서
```

---

## 핵심 도메인 모델

### 1. Radar 도메인
- **RadarPlayer**: 감시 대상 35개 사업자 (5개 레이어: `FRONTIER_LAB`, `CSP_PLATFORM`, `CONSULTING`, `GLOBAL_SI_MSP`, `KOREA_SI_MSP`)
- **RadarSignal**: 검증된 산업 신호 (사실 요약, 출처 URL, 티어 `TIER_1`/`TIER_2`, 신호 유형, 신뢰도/영향도 점수, 6개 Lens 연결)
- **RadarAssessment**: MSP 관점의 심층 영향 분석 (변화점, 산업 구조 영향, MSP 기회/위협, 구조적 리스크, 권고 행동, 딜리버리/과금 모델)
- **RadarWeeklyBrief**: 최근 7일 신호를 종합한 주간 브리핑 (경영진 요약, 플레이어 이동, 파트너십, 딜리버리/과금/운영 모델 변화, 한국 시장 영향)
- **6개 Radar Lens**:
  - `AI_AGENT`: AI Agent 제품·플랫폼과 자율 업무 실행 구조
  - `FRONTIER_LABS`: 모델 경쟁력과 생태계 지배력
  - `PARTNERSHIP`: 모델사·클라우드·SI의 결합 및 판매 구조
  - `DEPLOYMENT_MODEL`: FDE·RDE·ODE 등 현장 투입형 AI 딜리버리
  - `AI_PRICING`: 사용량·성과형 과금 전환, 토큰 단가 및 비용 체계 개편
  - `AGENTIC_OPERATIONS`: AIOps·운영 자동화와 관리형 서비스(Agentic ITO) 재편

### 2. Article 도메인
- 수집된 원문 기사 및 3줄 요약, 관련도 점수(0~100), 카테고리, 경쟁사 태깅, 북마크/메모, AI 분석 상태(`PENDING`, `COMPLETED`, `REJECTED`) 및 Radar 분석 상태(`PENDING`, `COMPLETED`, `NOT_TARGET`, `IRRELEVANT`) 관리

### 3. Insight & BattleCard 도메인
- **Insight**: 기회(`OPPORTUNITY`), 위협(`THREAT`), 트렌드(`TREND`), 전략(`STRATEGY`) 도출 및 근거 기사 연결
- **BattleCard**: 주요 경쟁사(LG CNS, SK AX, 베스핀글로벌, 삼일PwC)별 SWOT 및 우리 팀의 대응 전략

---

## 자동 스케줄 (SchedulerConfig.java — Asia/Seoul)

| 스케줄 | cron / 주기 | 실행 작업 |
|---|---|---|
| 매일 KST 01:00 | `0 0 1 * * *` | 정기 원문 기사 수집 (`CrawlerOrchestrator.crawlAll()`) |
| 매일 KST 02:00 | `0 0 2 * * *` | 전략 인사이트 생성 (`InsightService.generateInsights()`) |
| 매일 KST 01:30, 07:30, 13:30, 19:30 (하루 4회) | `0 30 1,7,13,19 * * *` | 보류 기사 AI 재분석 및 Radar Signal 심층 처리 (`ArticleAnalysisRetryService`, `RadarCollectionService`) |
| 매주 월·수 KST 07:00 | `0 0 7 * * MON,WED` | 데일리 브리핑 자동 생성 (`StrategyReportService.generateReport()`) |
| 매 1시간 주기 | `fixedDelay = 3600000` | 최근 Radar Signal 출처 URL 접근성(404/410) 재검증 (`RadarSourceVerificationService`) |

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API 키 | 필수 |
| `GEMINI_MODEL` | Gemini 모델 ID | `gemini-3.5-flash-lite` |
| `GEMINI_RATE_LIMIT_MS` | Gemini API 최소 호출 간격(ms) | `10000` |
| `CRAWL_NAVER_RESULT_LIMIT` | 검색어당 Naver 뉴스 수집 건수 | `10` |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | Naver 검색 API 인증 정보 | 선택 |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | 운영 PostgreSQL 연결 정보 | 운영 필수 |
| `SPRING_PROFILES_ACTIVE` | `prod` 시 PostgreSQL 연결 | 기본(H2) |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 오리진 목록 | localhost + Vercel |
| `API_SECRET_TOKEN` | 변조성 API 보호 토큰 (`X-API-Token`) | 미설정 시 비활성 |

---

## 주요 API 엔드포인트

### 1. Industry Radar (`/api/radar`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/radar/overview` | Watch List, 최근 신호, 6개 Lens 통계, 주간 브리핑 통합 조회 |
| GET | `/api/radar/signals` | 검증된 신호 목록 조회 (params: lens, minimumImpactScore, page, size) |
| POST | `/api/radar/signals` | 신규 검증 신호 수동 등록 🔒 |
| GET | `/api/radar/players` | Watch List 플레이어 목록 조회 |
| PUT | `/api/radar/players/{id}` | Watch List 플레이어 활성/우선순위 수정 🔒 |
| POST | `/api/radar/collect` | Radar 백그라운드 수집·분석 작업 시작 🔒 |
| GET | `/api/radar/collect/status` | Radar 수집 작업 진행 상태 조회 |
| POST | `/api/radar/collect/cancel` | Radar 수집 작업 취소 요청 🔒 |
| POST | `/api/radar/weekly-briefs/generate` | 주간 브리핑 수동 생성 🔒 |

### 2. Daily Briefings (`/api/strategy-reports`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/strategy-reports` | 데일리 브리핑 목록 페이징 조회 (params: page, size) |
| GET | `/api/strategy-reports/latest` | 최신 데일리 브리핑 1건 조회 |
| GET | `/api/strategy-reports/{id}` | 데일리 브리핑 단건 상세 조회 |
| POST | `/api/strategy-reports/generate` | 데일리 브리핑 수동 AI 생성 실행 🔒 |

### 3. Articles (`/api/articles`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/articles` | 페이지네이션 기사 목록 (params: competitor, category, sourceType, keyword, dateFrom, dateTo, page, size) |
| GET | `/api/articles/bookmarked` | 북마크 기사 목록 |
| GET | `/api/articles/{id}` | 기사 상세 (originalContent 포함) |
| PUT | `/api/articles/{id}/bookmark` | 북마크 토글 및 메모 수정 |
| POST | `/api/articles/crawl` | 기사 수집 백그라운드 작업 시작 🔒 |
| GET | `/api/articles/crawl/status` | 기사 수집 진행 상태 조회 |
| POST | `/api/articles/crawl/cancel` | 기사 수집 작업 취소 요청 🔒 |
| GET | `/api/articles/stats` | 대시보드 통계 지표 |

### 4. Insights (`/api/insights`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/insights` | 페이지네이션 인사이트 목록 |
| GET | `/api/insights/bookmarked` | 북마크 인사이트 목록 |
| GET | `/api/insights/{id}` | 인사이트 상세 (연관 근거 기사 목록 포함) |
| PUT | `/api/insights/{id}/bookmark` | 북마크 토글 및 메모 수정 |
| POST | `/api/insights/generate` | 인사이트 수동 생성 🔒 |

### 5. BattleCards & Trend News & Sources & Admin
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/battlecards` | 4대 경쟁사별 최신 배틀카드 |
| GET | `/api/battlecards/{competitor}` | 특정 경쟁사 배틀카드 이력 |
| GET | `/api/battlecards/detail/{id}` | 배틀카드 상세 |
| POST | `/api/battlecards/generate` | 배틀카드 수동 생성 🔒 |
| GET | `/api/trend-news` | 트렌드 뉴스 목록 조회 |
| POST | `/api/trend-news/generate` | 트렌드 뉴스 분석 수동 생성 🔒 |
| GET | `/api/sources` | 수집 소스 목록 조회 |
| POST | `/api/sources` | 수집 소스 추가 🔒 |
| PUT | `/api/sources/{id}/toggle` | 수집 소스 활성/비활성 토글 🔒 |
| GET | `/api/admin/config` | 시스템 환경 설정 조회 |
| PUT | `/api/admin/config` | 시스템 환경 설정 변경 🔒 |

> 🔒 = `X-API-Token` 헤더 필요 (`API_SECRET_TOKEN` 설정 시 보호)
