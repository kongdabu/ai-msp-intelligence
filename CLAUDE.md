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
- Java 21, Spring Boot 3.2, Gradle
- JPA/Hibernate, Lombok
- OkHttp3 (Gemini API 호출), Jsoup + Rome (크롤링)
- DB: 로컬 H2 (기본) / Supabase PostgreSQL (prod)
- AI: Google Gemini API (`gemini-2.5-flash-lite`)

### Frontend
- React 18, TypeScript strict mode, Vite
- TailwindCSS, Radix UI, Recharts
- TanStack Query, Zustand, React Router v6, Axios

---

## 프로젝트 구조

```
ai-msp-intelligence/
├── backend/                          # Spring Boot API 서버
│   ├── src/main/java/com/aimsp/intelligence/
│   │   ├── ai/
│   │   │   ├── GeminiApiClient.java  # Gemini API 호출 (Rate Limiting 포함, 4초 간격)
│   │   │   ├── SummaryGenerator.java # 기사 요약 생성 (JSON 파싱)
│   │   │   └── InsightGenerator.java # 전략 인사이트 생성
│   │   ├── crawler/
│   │   │   ├── CrawlerOrchestrator.java  # 전체 크롤링 조율 (5개 병렬 + RSS 순차)
│   │   │   ├── RssCrawler.java           # RSS 피드 수집
│   │   │   └── sources/                  # 경쟁사별 전용 크롤러
│   │   │       ├── LgCnsCrawler.java
│   │   │       ├── SkAxCrawler.java
│   │   │       ├── BespinCrawler.java
│   │   │       ├── PwcCrawler.java
│   │   │       └── ZdnetKoreaCrawler.java
│   │   ├── domain/
│   │   │   ├── article/   # Article 엔티티, Controller, Service, Repository
│   │   │   ├── insight/   # Insight 엔티티, Controller, Service, Repository
│   │   │   └── source/    # Source 엔티티, Controller, Service, Repository
│   │   ├── config/
│   │   │   ├── AppConfig.java       # Gemini 설정값 바인딩
│   │   │   ├── CorsConfig.java
│   │   │   └── SchedulerConfig.java
│   │   ├── dto/           # ArticleDto, InsightDto, SourceDto, DashboardDto
│   │   └── exception/     # GlobalExceptionHandler, GeminiApiUnavailableException
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
│   ├── vercel.json       # /api/* → https://aimsp-backend.onrender.com/api/* 프록시
│   └── Dockerfile        # nginx 기반
│
├── render.yaml           # Render 배포 설정 (backend Docker)
├── docker-compose.yml    # 로컬 전체 스택 (postgres + backend + frontend + nginx)
└── nginx/nginx.conf      # 로컬 리버스 프록시
```

---

## 주요 동작 흐름

### 크롤링 (`POST /api/articles/crawl`)
1. Gemini API 헬스체크 (토큰 소비 없음)
2. 5개 경쟁사 크롤러 병렬 실행 (스레드풀 3개)
3. 수집된 기사 순차 저장 — 중복 URL은 Gemini 호출 없이 스킵
4. 신규 기사만 `SummaryGenerator`로 Gemini 요약 생성 (4초 간격 Rate Limit)
5. DB 저장

### 인사이트 생성 (`POST /api/insights/generate`)
- 최근 기사 최대 15건을 `InsightGenerator`로 전달
- Gemini가 4건 이하 전략 인사이트 JSON 반환
- Rate Limiting은 `GeminiApiClient` 레벨에서 전역 적용 (SummaryGenerator 공유)

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API 키 | 필수 |
| `DB_URL` | Supabase JDBC URL (`prod` 프로파일) | 필수 (prod) |
| `DB_USERNAME` | DB 사용자명 | 필수 (prod) |
| `DB_PASSWORD` | DB 비밀번호 | 필수 (prod) |
| `SPRING_PROFILES_ACTIVE` | `prod` 설정 시 Supabase 사용 | `default` (H2) |
| `CRAWL_INTERVAL_HOURS` | 크롤링 간격 (현재 Manual 운영) | 24 |
| `INSIGHT_CRON` | 인사이트 생성 크론 (현재 Manual 운영) | `0 0 7 * * ?` |

> `CRAWL_INTERVAL_HOURS`와 `INSIGHT_CRON`은 현재 Manual 수행 — 안정화 후 자동화 예정

---

## 코딩 규칙
- Java 21, Spring Boot 3.2, Lombok 필수
- TypeScript strict mode, 함수형 컴포넌트만 사용
- API: RESTful JSON, 한국어 주석 허용
- 에러: GlobalExceptionHandler 전역 처리
- 크롤링: robots.txt 준수, 요청 간격 Manual 처리

---

## DB 설정

### 로컬 (기본 프로파일)
- H2 in-memory: `jdbc:h2:file:./data/aimspdb`
- `ddl-auto: update`, H2 콘솔: `/h2-console`

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
