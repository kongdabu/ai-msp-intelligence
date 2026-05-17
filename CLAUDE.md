# AI MSP Intelligence Platform

## 목적
AI MSP 사업 전략팀용 경쟁사 동향 모니터링 플랫폼.
LG CNS, SK AX, 베스핀글로벌, PwC의 AI/AI Agent/ITO 관련 뉴스를
자동 수집·요약·분석하여 전략적 인사이트와 주간 전략 레포트를 제공한다.

## 비즈니스 컨텍스트
- 타겟: 한국 금융·공공 엔터프라이즈
- 서비스: AI MSP (구독 + 성과 기반 혼합)
- 핵심 차별화: AI Agent 기반 ITO 전환, Vertical Agent IP
- 웹 애플리케이션: https://ai-msp-intelligence.vercel.app
- 백엔드 API: https://aimsp-backend.onrender.com

---

## 기술 스택

### Backend
- Java 21, Spring Boot 3.3.5, Gradle
- JPA/Hibernate, Lombok
- OkHttp3 4.12.0 (Gemini API 호출), Jsoup 1.17.2 + Rome 2.1.0 (크롤링)
- DB: 로컬 H2 (기본) / Supabase PostgreSQL (prod)
- AI: Google Gemini API (`gemini-3.1-flash-lite`)
  - Rate Limit: 10 RPM → 10초 간격 강제
  - Retry: 3회, 지수 백오프 (429/503 오류 시 retry-after 헤더 파싱)
  - Timeout: connect 30s / read 120s / write 30s
  - 응답 형식: `application/json` 강제 (`responseMimeType`)
  - max-tokens: 4096

### Frontend
- React 18.2.0, TypeScript 5.2.2 strict mode, Vite 5.2.0
- TailwindCSS 3.4.3, Radix UI, Recharts 2.12.3
- TanStack Query 5.28.6 (staleTime: 5분), Zustand 4.5.2, React Router 6.22.3, Axios 1.6.8
- lucide-react 0.363.0, date-fns 3.6.0, clsx 2.1.0, tailwind-merge 2.2.2

### 주간 레포트 (Harness 에이전트 팀 + Python 스크립트)
- 데이터 수집: `scripts/fetch-weekly-data.py` (프로덕션 REST API)
- 전문가 분석: Claude Opus 에이전트 3인 팀 (AI MSP·ITO·IT 전략 전문가)
- Word 생성: `scripts/build-weekly-report-docx.py` (python-docx)

---

## 프로젝트 구조

```
ai-msp-intelligence/
├── backend/                          # Spring Boot API 서버
│   ├── src/main/java/com/aimsp/intelligence/
│   │   ├── AiMspApplication.java
│   │   ├── ai/
│   │   │   ├── GeminiApiClient.java        # Gemini API HTTP 클라이언트 (Rate Limiting, Retry)
│   │   │   ├── SummaryGenerator.java       # 기사 요약·카테고리·경쟁사·관련도 점수 생성
│   │   │   ├── InsightGenerator.java       # 전략 인사이트 생성 (sourceArticleIds 기반 연결)
│   │   │   └── BattleCardGenerator.java    # 경쟁사별 배틀카드(SWOT+전략) 생성
│   │   ├── crawler/
│   │   │   ├── CrawlerOrchestrator.java    # 전체 크롤링 조율 (병렬 + API 헬스체크)
│   │   │   ├── NaverNewsClient.java        # Naver 뉴스 검색 API 클라이언트
│   │   │   ├── RssCrawler.java             # RSS 3-phase 파싱 (Rome → Sanitized XML → Jsoup)
│   │   │   └── sources/                   # 경쟁사별 Google News RSS 크롤러
│   │   │       ├── LgCnsCrawler.java       # "LG CNS" 검색어
│   │   │       ├── SkAxCrawler.java        # "SK AX" 검색어
│   │   │       ├── BespinCrawler.java      # "베스핀글로벌" 검색어
│   │   │       ├── PwcCrawler.java         # "PwC" 검색어
│   │   │       └── ZdnetKoreaCrawler.java  # ZDNet Korea RSS (일반 기술 뉴스)
│   │   ├── domain/
│   │   │   ├── article/       # Article 엔티티, Controller, Service, Repository, DashboardController
│   │   │   ├── insight/       # Insight 엔티티, Controller, Service, Repository
│   │   │   ├── battlecard/    # BattleCard 엔티티, Controller, Service, Repository
│   │   │   ├── weeklyreport/  # WeeklyReport 엔티티, Controller, Service, Repository
│   │   │   ├── source/        # Source 엔티티, Controller, Service, Repository
│   │   │   └── config/        # SystemConfig 싱글톤 설정 엔티티 (인사이트 생성 파라미터)
│   │   ├── config/
│   │   │   ├── AppConfig.java       # Gemini·Naver·Report 설정값 바인딩
│   │   │   ├── CorsConfig.java
│   │   │   └── SchedulerConfig.java # 자동 스케줄 (4개 작업)
│   │   ├── dto/           # ArticleDto, InsightDto, BattleCardDto, WeeklyReportDto,
│   │   │                  # SourceDto, DashboardDto, SystemConfigDto
│   │   └── exception/     # GlobalExceptionHandler, AiApiUnavailableException
│   ├── src/main/resources/
│   │   ├── application.yml           # 환경 설정 (default: H2, prod: Supabase)
│   │   ├── data.sql                  # 로컬 초기 데이터
│   │   ├── data-prod.sql             # prod 초기 소스 데이터 (ON CONFLICT DO NOTHING)
│   │   └── db/migration/
│   │       ├── V2__fix_bytea_columns.sql
│   │       └── V3__insight_articles_add_relevance_score.sql
│   ├── data/                         # H2 파일 DB (로컬 전용)
│   └── Dockerfile                    # eclipse-temurin:21, multi-stage build
│
├── frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/        # Dashboard, Articles, Insights, Competitors,
│   │   │                 # Battlecards, Sources, Settings
│   │   ├── components/
│   │   │   ├── article/  # ArticleCard, ArticleFilter, ArticleList
│   │   │   ├── insight/  # InsightCard, InsightPanel
│   │   │   ├── battlecard/ # BattlecardPanel
│   │   │   ├── dashboard/  # TrendChart, CompetitorDonut, KeywordCloud
│   │   │   └── layout/   # Header, Sidebar
│   │   ├── hooks/        # useArticles, useInsights, useBattlecards,
│   │   │                 # useDashboard, useSystemConfig
│   │   ├── store/        # filterStore (Zustand)
│   │   └── types/        # index.ts 공통 타입
│   ├── vite.config.ts    # dev proxy: /api → http://localhost:8080
│   ├── vercel.json       # /api/* → https://aimsp-backend.onrender.com/api/* 프록시
│   └── Dockerfile        # nginx 기반
│
├── scripts/
│   ├── fetch-weekly-data.py          # 프로덕션 API → _workspace 데이터 수집
│   └── build-weekly-report-docx.py  # 전문가 분석 결과 → Word 레포트 생성
│
├── _workspace/
│   └── weekly_{YYYY-MM-DD}/          # 에이전트 팀 작업 공간
│       ├── articles.json             # 수집된 기사
│       ├── insights.json             # 수집된 인사이트
│       ├── meta.json                 # 수집 메타 정보
│       ├── ai_msp_analysis.json      # AI MSP 전문가 분석
│       ├── ito_analysis.json         # ITO 전문가 분석
│       └── it_strategy_analysis.json # IT 전략 전문가 분석
│
├── reports/
│   └── weekly/                       # 생성된 Word 레포트 저장 디렉토리
│       └── YYYY-MM-DD_ai-msp-weekly-report.docx
│
├── render.yaml           # Render 배포 설정 (backend Docker)
├── docker-compose.yml    # 로컬 전체 스택 (postgres 15 + backend + frontend + nginx)
└── nginx/nginx.conf      # 로컬 리버스 프록시
```

---

## 도메인 모델

### Article 엔티티
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long (PK) | 자동 증가 |
| `url` | String (2000, unique) | 기사 URL |
| `title` | String (500) | 기사 제목 |
| `originalContent` | TEXT (max 5000 chars) | 원문 내용 |
| `summary` | String (500) | AI 생성 요약 (max 200자) |
| `competitor` | String (50) | `LG_CNS` \| `SK_AX` \| `BESPIN` \| `PWC` \| `GENERAL` |
| `category` | String (50) | `AI_AGENT` \| `VERTICAL_AI` \| `ITO` \| `MSP` \| `CLOUD` \| `GEN_AI` |
| `sourceType` | String (50) | `NEWS` \| `HOMEPAGE` \| `SNS` \| `IDC_REPORT` |
| `sourceName` | String (200) | 출처명 |
| `publishedAt` | LocalDateTime | 기사 발행일 |
| `collectedAt` | LocalDateTime | 수집일시 |
| `isProcessed` | Boolean (default: false) | 인사이트 생성 처리 여부 |
| `relevanceScore` | Integer (0-100) | AI 관련도 점수 |

인덱스: `(competitor, published_at)`, `collected_at`, `published_at`, `is_processed`

### Insight 엔티티
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long (PK) | 자동 증가 |
| `title` | String (200) | 인사이트 제목 |
| `content` | TEXT | 인사이트 내용 (max 200자) |
| `insightType` | String (50) | `OPPORTUNITY` \| `THREAT` \| `TREND` \| `STRATEGY` |
| `competitor` | String (50) | 관련 경쟁사 |
| `impactScore` | Integer (1-5) | 영향도 점수 |
| `actionItems` | List\<String\> (각 500자, 최대 2개) | 대응 액션 아이템 |
| `sourceArticles` | OneToMany → InsightArticle | 근거 기사 (relevance_score 포함) |
| `generatedAt` | LocalDateTime | 생성일시 |

인덱스: `generated_at`, `competitor`, `impact_score`

### BattleCard 엔티티
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long (PK) | 자동 증가 |
| `competitor` | String (50) | `LG_CNS` \| `SK_AX` \| `BESPIN` \| `PWC` |
| `strengths` | TEXT | 경쟁사 강점 (JSON 배열) |
| `weaknesses` | TEXT | 경쟁사 약점 (JSON 배열) |
| `opportunities` | TEXT | 기회 (JSON 배열) |
| `threats` | TEXT | 위협 (JSON 배열) |
| `ourStrategy` | TEXT | 우리의 대응 전략 |
| `impactScore` | Integer (1-5) | 영향도 점수 |
| `sourceArticleCount` | int (@Formula) | 근거 기사 수 (서브쿼리, N+1 방지) |
| `sourceArticles` | OneToMany → BattleCardArticle | 근거 기사 |
| `generatedAt` | LocalDateTime | 생성일시 |

인덱스: `competitor`, `generated_at`

### WeeklyReport 엔티티
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long (PK) | 자동 증가 |
| `title` | String (200) | 레포트 제목 |
| `competitorTrends` | TEXT | 경쟁사 동향 (JSON) |
| `aiTrends` | TEXT | AI 사업 Trend (JSON) |
| `strategyRecommendations` | TEXT | AI MSP 추진 전략 (JSON) |
| `weekStart` | LocalDate | 대상 주 시작일 (월요일) |
| `weekEnd` | LocalDate | 대상 주 종료일 (일요일) |
| `articleCount` | Integer | 참조 기사 수 |
| `insightCount` | Integer | 참조 인사이트 수 |
| `docxPath` | String (500) | 생성된 Word 파일 절대 경로 |
| `generatedAt` | LocalDateTime | 생성일시 |

인덱스: `week_start`

### SystemConfig 엔티티 (싱글톤, id=1 고정)
| 필드 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `maxArticlesForInsight` | int | 150 | 인사이트 생성 시 입력 기사 최대 수 |
| `maxInsightsPerGeneration` | int | 8 | 인사이트 생성 최대 건수 |

### Source 엔티티
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | Long (PK) | 자동 증가 |
| `name` | String (200) | 소스명 |
| `url` | String (500, unique) | RSS/URL |
| `type` | String (50) | `NEWS` \| `HOMEPAGE` \| `SNS` \| `IDC` |
| `competitor` | String (50) | 연관 경쟁사 |
| `active` | Boolean (default: true) | 활성 여부 |
| `lastCrawledAt` | LocalDateTime | 마지막 크롤링 일시 |
| `crawlCount` | Integer | 총 크롤링 횟수 |
| `errorCount` | Integer | 에러 횟수 |

---

## 자동 스케줄 (SchedulerConfig.java — Asia/Seoul)

| 스케줄 | cron | 작업 |
|---|---|---|
| 매일 KST 01:00 | `0 0 1 * * *` | 기사 수집 (`CrawlerOrchestrator`) |
| 매일 KST 02:00 | `0 0 2 * * *` | 인사이트 생성 (`InsightService`) |
| 매주 월요일 KST 03:00 | `0 0 3 * * MON` | 배틀카드 생성 (`BattleCardService`) |
| 매주 월요일 KST 05:00 | Claude Code `/schedule` | 주간 전략 레포트 (Harness 에이전트 팀) |

---

## 주요 동작 흐름

### 크롤링 (`POST /api/articles/crawl`)
1. Gemini API 헬스체크 (토큰 소비 없음, `GET /models/{model}`)
2. 경쟁사별 크롤러 병렬 실행 (스레드풀)
3. 수집된 기사 순차 저장 — 중복 URL은 Gemini 호출 없이 스킵
4. 신규 기사만 `SummaryGenerator`로 Gemini 요약 생성 (10초 간격 Rate Limit)
5. DB 저장

### 인사이트 생성 (`POST /api/insights/generate`)
- `isProcessed=false` 기사를 SystemConfig 설정 수만큼(`maxArticlesForInsight`) 조회
- `InsightGenerator`로 Gemini 호출 → 최대 `maxInsightsPerGeneration`건 JSON 반환
- `sourceArticleIds`로 기사 역참조 → `InsightArticle` 연결 저장
- Gemini 0건 반환 시 기사 `isProcessed` 마킹 보류 (다음 생성 시 재사용)

### 배틀카드 생성 (`POST /api/battlecards/generate`)
- 경쟁사 4개(LG_CNS, SK_AX, BESPIN, PWC)별로 최근 기사 20건씩 조회
- `BattleCardGenerator`로 Gemini 호출 → SWOT + 대응 전략 JSON 반환
- 경쟁사별 배틀카드 저장

### 주간 전략 레포트 생성 (Harness 에이전트 팀)
1. **데이터 수집**: `scripts/fetch-weekly-data.py` → 프로덕션 API에서 기사·인사이트 수집 → `_workspace/weekly_{date}/`
2. **전문가 병렬 분석**: Claude Opus 에이전트 3인 동시 실행
   - AI MSP 전문가 → `ai_msp_analysis.json`
   - ITO 전문가 → `ito_analysis.json`
   - IT 전략 전문가 → `it_strategy_analysis.json`
3. **레포트 통합**: `scripts/build-weekly-report-docx.py` → 3개 분석 통합 → Word 파일 생성
4. 매주 월요일 `weekly-report-orchestrate` 스킬로 실행

### RSS 파싱 3-phase (`RssCrawler.java`)
1. **Phase 1**: Rome XML 파서 직접 파싱 (정상 피드)
2. **Phase 2**: HTML 보이드 요소 → self-closing 변환 후 Rome 재시도 (비표준 피드)
3. **Phase 3**: Jsoup fallback (심하게 깨진 피드)

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API 키 | 필수 |
| `NAVER_CLIENT_ID` | Naver 검색 API Client ID | 선택 |
| `NAVER_CLIENT_SECRET` | Naver 검색 API Client Secret | 선택 |
| `DB_URL` | Supabase JDBC URL (`prod` 프로파일) | 필수 (prod) |
| `DB_USERNAME` | DB 사용자명 | 필수 (prod) |
| `DB_PASSWORD` | DB 비밀번호 | 필수 (prod) |
| `SPRING_PROFILES_ACTIVE` | `prod` 설정 시 Supabase 사용 | `default` (H2) |
| `REPORT_DIRECTORY` | Word 레포트 저장 경로 | `reports/weekly` |

---

## 코딩 규칙
- Java 21, Spring Boot 3.3.5, Lombok 필수
- TypeScript strict mode, 함수형 컴포넌트만 사용
- API: RESTful JSON, 한국어 주석 허용
- 에러: `GlobalExceptionHandler` 전역 처리 (`AiApiUnavailableException` 포함)
- 크롤링: robots.txt 준수, 10초 간격 Rate Limit 준수
- Gemini 응답은 항상 `application/json` 형식으로 강제 (`responseMimeType`)
- BattleCard의 sourceArticleCount는 `@Formula` 서브쿼리로 계산 (N+1 방지)

---

## DB 설정

### 로컬 (기본 프로파일)
- H2 파일 DB: `jdbc:h2:file:./data/aimspdb;AUTO_SERVER=TRUE`
- `ddl-auto: update`, H2 콘솔: `/h2-console`
- 로컬 개발 시 `data.sql`로 초기 소스 데이터 자동 투입

### 운영 (`prod` 프로파일)
- Supabase PostgreSQL
- `ddl-auto: validate` (스키마 자동 변경 없음)
- Flyway: `baseline-version: 6`, `ignore-missing-migrations: true`
- HikariCP `maximum-pool-size: 5` (Supabase free tier 대응)
- URL 형식: `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require`

---

## 호스팅

| 구분 | 서비스 | URL |
|---|---|---|
| GitHub | 소스 저장소 | https://github.com/kongdabu/ai-msp-intelligence |
| Backend | Render (Docker) | https://aimsp-backend.onrender.com |
| Frontend | Vercel (Vite) | https://ai-msp-intelligence.vercel.app |

### Render 배포
- `render.yaml` 기반 자동 배포 (GitHub main 푸시 시 트리거)
- 환경변수: Render 대시보드에서 직접 설정
- 헬스체크: `/actuator/health`

### Vercel 배포 (Frontend)
- Root Directory: `frontend` / Build: `npm run build` / Output: `dist`
- `/api/*` → `vercel.json`으로 Render 백엔드 자동 프록시

---

## 전체 API 엔드포인트

### Articles (`/api/articles`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/articles` | 페이지네이션 목록 (params: competitor, category, sourceType, keyword, dateFrom, dateTo, page, size) |
| GET | `/api/articles/list` | 비페이지네이션 목록 (Competitors 페이지용, params: competitor, category, dateFrom, dateTo, limit=50) |
| GET | `/api/articles/{id}` | 기사 상세 (originalContent 포함) |
| POST | `/api/articles/crawl` | 크롤링 수동 실행 |
| GET | `/api/articles/stats` | 통계 (todayCount, byCompetitor, categoryTrend) |

### Insights (`/api/insights`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/insights` | 페이지네이션 목록 (params: type, competitor, page, size) |
| GET | `/api/insights/{id}` | 인사이트 상세 (sourceArticles 배열 포함) |
| POST | `/api/insights/generate` | 인사이트 생성 수동 실행 |
| GET | `/api/insights/today` | 오늘 생성된 인사이트 목록 |

### BattleCards (`/api/battlecards`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/battlecards` | 경쟁사별 최신 배틀카드 4건 |
| GET | `/api/battlecards/{competitor}` | 특정 경쟁사 배틀카드 이력 (최근 10건) |
| GET | `/api/battlecards/detail/{id}` | 배틀카드 상세 (출처 기사 포함) |
| POST | `/api/battlecards/generate` | 배틀카드 수동 생성 |

### Weekly Reports (`/api/weekly-reports`)
| Method | Path | 설명 |
|---|---|---|
| POST | `/api/weekly-reports/generate` | 주간 레포트 생성 (DB 저장 + Word 파일 생성) |
| GET | `/api/weekly-reports` | 최근 레포트 목록 (최대 10건) |
| GET | `/api/weekly-reports/{id}/download` | Word 파일 다운로드 |

### Sources (`/api/sources`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/sources` | 전체 소스 목록 |
| POST | `/api/sources` | 소스 추가 |
| PUT | `/api/sources/{id}/toggle` | 소스 활성/비활성 토글 |

### Dashboard & Admin
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/dashboard/summary` | KPI, 경쟁사 분포, 카테고리 트렌드, 최신 인사이트/기사 |
| GET | `/api/admin/config` | SystemConfig 조회 (인사이트 생성 파라미터) |
| PUT | `/api/admin/config` | SystemConfig 수정 |

---

## 하네스: AI MSP 주간 전략 레포트

**목표:** 지난주 수집 기사·인사이트를 기반으로 AI MSP 전략 주간 레포트(MS Word)를 자동 생성한다.

**트리거:** "주간 레포트", "weekly report", "레포트 생성", "레포트 재실행", "레포트 업데이트" 요청 시 `weekly-report-orchestrate` 스킬을 사용하라. 레포트 즉시 실행만 원하면 `weekly-report-run` 스킬을 직접 사용 가능.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-05-17 | 초기 구성 | 전체 | 개별 인사이트 중심 한계 → 전략적 주간 레포트 필요 |
| 2026-05-18 | Gemini 생성 제거, 에이전트 팀 방식으로 전환 | 전체 | AI MSP·ITO·IT전략 전문가 3인 병렬 분석으로 품질 향상 |
