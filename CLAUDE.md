# AI MSP Intelligence Platform

## 목적
AI MSP 사업 전략팀용 경쟁사 동향 모니터링 플랫폼.
LG CNS, SK AX, 베스핀글로벌, PwC의 AI/AI Agent/ITO 관련 뉴스를
자동 수집·요약·분석하여 전략적 인사이트를 제공한다.

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
│   │   │       ├── LgCnsCrawler.java
│   │   │       ├── SkAxCrawler.java
│   │   │       ├── BespinCrawler.java
│   │   │       ├── PwcCrawler.java
│   │   │       └── ZdnetKoreaCrawler.java
│   │   ├── domain/
│   │   │   ├── article/   # Article 엔티티, Controller, Service, Repository, DashboardController
│   │   │   ├── insight/   # Insight 엔티티, Controller, Service, Repository
│   │   │   ├── battlecard/ # BattleCard 엔티티, Controller, Service, Repository
│   │   │   ├── source/    # Source 엔티티, Controller, Service, Repository
│   │   │   └── config/    # SystemConfig 싱글톤 설정 엔티티
│   │   ├── config/
│   │   │   ├── AppConfig.java       # Gemini·Naver·CORS 설정값 바인딩
│   │   │   ├── ApiTokenFilter.java  # X-API-Token 변조성 엔드포인트 보호
│   │   │   ├── CorsConfig.java
│   │   │   └── SchedulerConfig.java # 자동 스케줄 (3개 작업)
│   │   ├── dto/           # ArticleDto, InsightDto, BattleCardDto, SourceDto, DashboardDto, SystemConfigDto
│   │   └── exception/     # GlobalExceptionHandler, AiApiUnavailableException
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── data.sql
│   │   ├── data-prod.sql
│   │   └── db/migration/  # V2~V4, V10(weekly_report 테이블 제거)
│   └── Dockerfile
│
├── frontend/                         # React SPA
│   ├── src/
│   │   ├── pages/        # Dashboard, Articles, Insights, Competitors, Battlecards, Sources, Settings
│   │   ├── components/   # article/, insight/, battlecard/, dashboard/, layout/
│   │   ├── hooks/        # useArticles, useInsights, useBattlecards, useDashboard, useSystemConfig
│   │   ├── store/        # filterStore (Zustand)
│   │   └── types/        # index.ts 공통 타입
│   ├── vite.config.ts
│   ├── vercel.json       # /api/* → Render 백엔드 프록시
│   └── Dockerfile
│
├── render.yaml
├── docker-compose.yml
└── nginx/nginx.conf
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
| `bookmarked` | Boolean (default: false) | 저장(북마크) 여부 — 나중에 다시 조회 |
| `bookmarkedAt` | LocalDateTime | 저장한 일시 (해제 시 null) |
| `bookmarkNote` | String (500) | 리마인드용 메모 (저장 시 작성) |

인덱스: `generated_at`, `competitor`, `impact_score`, `bookmarked`

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
| `CORS_ALLOWED_ORIGINS` | CORS 허용 오리진 (콤마 구분) | localhost + Vercel 도메인 |
| `API_SECRET_TOKEN` | 변조성 API 보호 토큰 | 선택 (미설정 시 비활성) |

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
| GET | `/api/articles/list` | 비페이지네이션 목록 (params: competitor, category, dateFrom, dateTo, limit=50) |
| GET | `/api/articles/{id}` | 기사 상세 (originalContent 포함) |
| POST | `/api/articles/crawl` | 크롤링 수동 실행 🔒 |
| GET | `/api/articles/stats` | 통계 (todayCount, byCompetitor, categoryTrend) |

### Insights (`/api/insights`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/insights` | 페이지네이션 목록 (params: type, competitor, page, size) |
| GET | `/api/insights/bookmarked` | 저장(북마크)한 인사이트 목록 (최근 저장순, params: page, size) |
| GET | `/api/insights/{id}` | 인사이트 상세 (sourceArticles 배열 포함) |
| PUT | `/api/insights/{id}/bookmark` | 인사이트 저장/해제 토글 + 메모 갱신 (body: bookmarked, note) |
| POST | `/api/insights/generate` | 인사이트 생성 수동 실행 🔒 |
| GET | `/api/insights/today` | 오늘 생성된 인사이트 목록 |

### BattleCards (`/api/battlecards`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/battlecards` | 경쟁사별 최신 배틀카드 4건 |
| GET | `/api/battlecards/{competitor}` | 특정 경쟁사 배틀카드 이력 (최근 10건) |
| GET | `/api/battlecards/detail/{id}` | 배틀카드 상세 (출처 기사 포함) |
| POST | `/api/battlecards/generate` | 배틀카드 수동 생성 🔒 |

### Sources (`/api/sources`)
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/sources` | 전체 소스 목록 |
| POST | `/api/sources` | 소스 추가 🔒 |
| PUT | `/api/sources/{id}/toggle` | 소스 활성/비활성 토글 |

### Dashboard & Admin
| Method | Path | 설명 |
|---|---|---|
| GET | `/api/dashboard/summary` | KPI, 경쟁사 분포, 카테고리 트렌드, 최신 인사이트/기사 |
| GET | `/api/admin/config` | SystemConfig 조회 |
| PUT | `/api/admin/config` | SystemConfig 수정 🔒 |

> 🔒 = `X-API-Token` 헤더 필요 (API_SECRET_TOKEN 환경변수 설정 시 활성)
