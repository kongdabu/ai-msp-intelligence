# AI MSP Intelligence Platform

## 목적
AI MSP 사업 전략팀용 경쟁사 동향 모니터링 플랫폼.
LG CNS, SK AX, 베스핀글로벌, PwC의 AI/AI Agent/ITO 관련 뉴스를
자동 수집·요약·분석하여 전략적 인사이트를 제공한다.

## 비즈니스 컨텍스트
- 타겟: 한국 금융·공공 엔터프라이즈
- 서비스: AI MSP (구독 + 성과 기반 혼합)
- 핵심 차별화: AI Agent 기반 ITO 전환, Vertical Agent IP

---

## 기술 스택

### Backend
- Java 21, Spring Boot 3.2.3, Gradle
- JPA/Hibernate, Lombok
- OkHttp3 4.12.0 (Gemini API 호출), Jsoup 1.17.2 + Rome 2.1.0 (크롤링)
- DB: 로컬 H2 (기본) / Supabase PostgreSQL (prod)
- AI: Google Gemini API (`gemini-2.5-flash-lite`)
  - Rate Limit: 15 RPM (4초 간격 강제)
  - Retry: 3회, 지수 백오프 (429/503 오류 시 retry-after 헤더 파싱)
  - Timeout: connect 30s / read 120s / write 30s
  - 응답 형식: `application/json` 강제 (`responseMimeType`)

### Frontend
- React 18.2.0, TypeScript 5.2.2 strict mode, Vite 5.2.0
- TailwindCSS 3.4.3, Radix UI, Recharts 2.12.3
- TanStack Query 5.28.6 (staleTime: 5분), Zustand 4.5.2, React Router 6.22.3, Axios 1.6.8
- lucide-react 0.363.0, date-fns 3.6.0, clsx 2.1.0

---

## 프로젝트 구조

```
ai-msp-intelligence/
├── backend/                          # Spring Boot API 서버
│   ├── src/main/java/com/aimsp/intelligence/
│   │   ├── AiMspApplication.java
│   │   ├── ai/
│   │   │   ├── GeminiApiClient.java  # Gemini API HTTP 클라이언트 (Rate Limiting, Retry 포함)
│   │   │   ├── SummaryGenerator.java # 기사 요약·카테고리·경쟁사·관련도 점수 생성
│   │   │   └── InsightGenerator.java # 전략 인사이트 생성 (sourceArticleIds 기반 연결)
│   │   ├── crawler/
│   │   │   ├── CrawlerOrchestrator.java  # 전체 크롤링 조율 (5개 병렬 + API 헬스체크)
│   │   │   ├── RssCrawler.java           # RSS 3-phase 파싱 (Rome → Sanitized XML → Jsoup)
│   │   │   └── sources/                  # 경쟁사별 Google News RSS 크롤러
│   │   │       ├── LgCnsCrawler.java     # "LG CNS" 검색어
│   │   │       ├── SkAxCrawler.java      # "SK AX" 검색어
│   │   │       ├── BespinCrawler.java    # "베스핀글로벌" 검색어
│   │   │       ├── PwcCrawler.java       # "PwC" 검색어
│   │   │       └── ZdnetKoreaCrawler.java# ZDNet Korea RSS (일반 기술 뉴스)
│   │   ├── domain/
│   │   │   ├── article/   # Article 엔티티, Controller, Service, Repository
│   │   │   │              # DashboardController도 여기 포함
│   │   │   ├── insight/   # Insight 엔티티, Controller, Service, Repository
│   │   │   └── source/    # Source 엔티티, Controller, Service, Repository
│   │   ├── config/
│   │   │   ├── AppConfig.java       # Gemini API 설정값 바인딩 (app.gemini.*)
│   │   │   ├── CorsConfig.java
│   │   │   └── SchedulerConfig.java # 자동 스케줄 (KST 01:00 크롤링, 02:00 인사이트)
│   │   ├── dto/           # ArticleDto, InsightDto, SourceDto, DashboardDto
│   │   └── exception/     # GlobalExceptionHandler, AiApiUnavailableException
│   ├── src/main/resources/
│   │   ├── application.yml           # 환경 설정 (default: H2, prod: Supabase)
│   │   ├── data.sql                  # 로컬 초기 데이터
│   │   ├── data-prod.sql             # prod 초기 소스 데이터 (ON CONFLICT DO NOTHING)
│   │   └── db/migration/
│   │       └── V2__fix_bytea_columns.sql  # bytea → text/varchar 수동 마이그레이션
│   └── Dockerfile                    # eclipse-temurin:21, multi-stage build
│
├── frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/        # Dashboard, Articles, Insights, Competitors, Sources
│   │   ├── components/
│   │   │   ├── article/  # ArticleCard, ArticleFilter, ArticleList
│   │   │   ├── insight/  # InsightCard, InsightPanel
│   │   │   ├── dashboard/# TrendChart, CompetitorDonut, KeywordCloud
│   │   │   └── layout/   # Header, Sidebar
│   │   ├── hooks/        # useArticles, useInsights, useDashboard
│   │   ├── store/        # filterStore (Zustand)
│   │   └── types/        # index.ts 공통 타입
│   ├── vite.config.ts    # dev proxy: /api → http://localhost:8080
│   ├── vercel.json       # /api/* → https://aimsp-backend.onrender.com/api/* 프록시
│   └── Dockerfile        # nginx 기반
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
| `summary` | String (500, max 200) | AI 생성 요약 |
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
| `content` | TEXT (max 200 chars) | 인사이트 내용 |
| `insightType` | String (50) | `OPPORTUNITY` \| `THREAT` \| `TREND` \| `STRATEGY` |
| `competitor` | String (50) | 관련 경쟁사 |
| `impactScore` | Integer (1-5) | 영향도 점수 |
| `actionItems` | List\<String\> (각 500자, 최대 2개) | 대응 액션 아이템 |
| `sourceArticles` | ManyToMany → Article | 근거 기사 (join table: `insight_articles`) |
| `generatedAt` | LocalDateTime | 생성일시 |

인덱스: `generated_at`, `competitor`, `impact_score`

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

## 주요 동작 흐름

### 크롤링 (`POST /api/articles/crawl`)
1. Gemini API 헬스체크 (토큰 소비 없음)
2. 5개 경쟁사 크롤러 병렬 실행 (스레드풀 3개)
3. 수집된 기사 순차 저장 — 중복 URL은 Gemini 호출 없이 스킵
4. 신규 기사만 `SummaryGenerator`로 Gemini 요약 생성 (4초 간격 Rate Limit)
5. DB 저장

### 인사이트 생성 (`POST /api/insights/generate`)
- `isProcessed=false` 기사 최대 15건을 `InsightGenerator`로 전달
- Gemini가 4건 이하 전략 인사이트 JSON 반환 (sourceArticleIds로 기사 연결)
- Rate Limiting은 `GeminiApiClient` 레벨에서 전역 적용 (SummaryGenerator/InsightGenerator 공유)

### 자동 스케줄 (`SchedulerConfig.java`)
- 크롤링: **매일 KST 01:00** 자동 실행 (`Asia/Seoul` timezone)
- 인사이트 생성: **매일 KST 02:00** 자동 실행

### RSS 파싱 3-phase (`RssCrawler.java`)
1. **Phase 1**: Rome XML 파서로 직접 파싱 (정상 피드)
2. **Phase 2**: HTML 보이드 요소 → self-closing 변환 후 Rome 재시도 (비표준 피드)
3. **Phase 3**: Jsoup fallback (심하게 깨진 피드)

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API 키 | 필수 |
| `DB_URL` | Supabase JDBC URL (`prod` 프로파일) | 필수 (prod) |
| `DB_USERNAME` | DB 사용자명 | 필수 (prod) |
| `DB_PASSWORD` | DB 비밀번호 | 필수 (prod) |
| `SPRING_PROFILES_ACTIVE` | `prod` 설정 시 Supabase 사용 | `default` (H2) |

---

## 코딩 규칙
- Java 21, Spring Boot 3.2.3, Lombok 필수
- TypeScript strict mode, 함수형 컴포넌트만 사용
- API: RESTful JSON, 한국어 주석 허용
- 에러: `GlobalExceptionHandler` 전역 처리 (`AiApiUnavailableException` 포함)
- 크롤링: robots.txt 준수, 4초 간격 Rate Limit 준수
- Gemini 응답은 항상 `application/json` 형식으로 강제하여 파싱 안정성 확보

---

## DB 설정

### 로컬 (기본 프로파일)
- H2 파일 DB: `jdbc:h2:file:./data/aimspdb;AUTO_SERVER=TRUE`
- `ddl-auto: update`, H2 콘솔: `/h2-console`
- 로컬 개발 시 `data.sql`로 초기 소스 데이터 자동 투입

### 운영 (`prod` 프로파일)
- Supabase PostgreSQL
- `ddl-auto: validate` (스키마 자동 변경 없음)
- `sql.init.mode: never` (data-prod.sql 재실행 방지)
- HikariCP `maximum-pool-size: 5` (Supabase free tier 대응)
- URL 형식: `jdbc:postgresql://db.xxxx.supabase.co:5432/postgres?sslmode=require`

---

## 호스팅

| 구분 | 서비스 | URL |
|---|---|---|
| GitHub | 소스 저장소 | https://github.com/kongdabu/ai-msp-intelligence |
| Backend | Render (Docker) | https://aimsp-backend.onrender.com |
| Frontend | Vercel (Vite) | Vercel 대시보드 확인 |

### Render 배포
- `render.yaml` 기반 자동 배포 (GitHub main 푸시 시 트리거)
- Render 대시보드에서 환경변수 직접 입력 필요: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `GEMINI_API_KEY`
- Render host ID: `dpg-d7dhpnho3t8c73d1tru0-a`
- 헬스체크: `/actuator/health`

### Vercel 배포 (Frontend)
- Root Directory: `frontend`
- Framework: Vite / Build: `npm run build` / Output: `dist`
- `/api/*` 요청은 `vercel.json`에 의해 Render 백엔드로 자동 프록시
- 별도 환경변수 설정 불필요

---

## 주요 API 엔드포인트

```bash
# 크롤링 수동 실행
curl -X POST https://aimsp-backend.onrender.com/api/articles/crawl

# 인사이트 생성
curl -X POST https://aimsp-backend.onrender.com/api/insights/generate

# 헬스체크
curl https://aimsp-backend.onrender.com/actuator/health
```

### 전체 엔드포인트 목록

#### Articles
- `GET /api/articles` — 페이지네이션 목록 (params: competitor, category, sourceType, keyword, dateFrom, dateTo, page, size)
- `GET /api/articles/list` — 비페이지네이션 목록 (Competitors 페이지 최적화, COUNT 쿼리 없음)
- `GET /api/articles/{id}` — 기사 상세 (originalContent 포함)
- `POST /api/articles/crawl` — 크롤링 수동 실행
- `GET /api/articles/stats` — 통계 (todayCount, byCompetitor, categoryTrend)

#### Insights
- `GET /api/insights` — 페이지네이션 목록 (params: type, competitor, page, size)
- `GET /api/insights/{id}` — 인사이트 상세 (sourceArticles 배열 포함)
- `POST /api/insights/generate` — 인사이트 생성 수동 실행
- `GET /api/insights/today` — 오늘 생성된 인사이트

#### Sources
- `GET /api/sources` — 전체 소스 목록
- `PUT /api/sources/{id}/toggle` — 소스 활성/비활성 토글
- `POST /api/sources` — 소스 추가

#### Dashboard
- `GET /api/dashboard/summary` — KPI, 경쟁사 분포, 카테고리 트렌드, 최신 인사이트/기사
