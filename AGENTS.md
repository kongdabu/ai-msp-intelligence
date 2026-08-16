# AI MSP Intelligence Platform

## 목적

한국 금융·공공 엔터프라이즈 시장을 대상으로 하는 AI 전략팀의 AI 생태계·서비스 사업모델 모니터링 플랫폼이다. Frontier AI Labs, AI 플랫폼 사업자, 컨설팅펌, FDE·RDE·ODE와 파트너십, Agentic AI·AIOps 동향을 수집하고 Gemini로 분석한다.

## 기술 구성

- Backend: Java 21, Spring Boot 3.3.5, Gradle, JPA/Hibernate, Flyway
- Database: 로컬 H2 / 운영 Supabase PostgreSQL
- AI: Google Gemini API, OkHttp
- Frontend: React 18, TypeScript strict mode, Vite, Tailwind CSS, TanStack Query, Zustand
- 배포: Render(백엔드 Docker), Vercel(프런트엔드)

## 주요 구조

```
backend/src/main/java/com/aimsp/intelligence/
├── ai/           # Gemini 클라이언트와 요약·인사이트·배틀카드·트렌드 생성기
├── crawler/      # Naver 뉴스 기반 AI 생태계 수집과 백그라운드 작업 관리
├── config/       # Gemini, CORS, API 토큰, 스케줄 설정
├── domain/       # article, insight, battlecard, source, trend, config
├── dto/          # API 응답 DTO
└── exception/    # 전역 예외 처리

frontend/src/
├── components/   # article, insight, battlecard, dashboard, trend, layout, common
├── hooks/        # API 조회 훅
├── pages/        # 화면 컴포넌트
├── store/        # Zustand 상태
└── types/        # 공통 타입
```

## 핵심 동작

- 기사 수집: OpenAI·Anthropic·Gemini·AWS·Microsoft·엔비디아, Accenture·딜로이트·PwC, FDE·RDE·ODE·파트너십, Agentic AI·AIOps 관련 신규 기사를 수집하고 Gemini로 관심 주제를 분류한다. 수동 수집은 백그라운드 작업으로 실행하며 상태 API로 진행 상황을 조회한다.
- 인사이트: 미처리 기사를 기반으로 전략 인사이트와 근거 기사를 연결한다.
- 트렌드 뉴스: 시장 트렌드 기사와 AI 분석 결과를 제공한다.
- 자동 스케줄(Asia/Seoul): 기사 수집 매일 01:00, 인사이트 생성 매일 02:00, 기사 AI 분석·Radar Signal 처리 하루 4회.

## Gemini 설정

- 기본 모델: `gemini-3.5-flash-lite`
- API 형식: `v1beta/models/{model}:generateContent`
- 응답 형식: `application/json` (`responseMimeType`)
- 재시도: 429·503 응답에 최대 3회 재시도
- 호출 간격: `GEMINI_RATE_LIMIT_MS`로 제어하며 기본값은 10초

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
| `API_SECRET_TOKEN` | 변경 API 보호 토큰 | 미설정 시 비활성 |

## 개발 규칙

- Java는 Java 21과 Lombok을 사용한다.
- TypeScript는 strict mode를 유지하고 `any`를 사용하지 않는다.
- React는 함수형 컴포넌트로 작성하고, 공통 UI는 재사용 가능한 컴포넌트로 분리한다.
- 화면은 반응형으로 구현한다.
- API 응답 형식은 일관되게 유지하고, 예외는 `GlobalExceptionHandler`에서 처리한다.
- 데이터 변경이 여러 단계에 걸치면 트랜잭션을 적용한다.
- 크롤링은 robots.txt와 Gemini 호출 제한을 준수한다.
- 코드 주석과 문서는 한국어로 작성한다. 변수·함수명은 영어를 사용한다.

## 배포

- `main` 브랜치 푸시는 Render와 Vercel의 자동 배포를 트리거한다.
- Render 헬스체크 엔드포인트: `/actuator/health`
- Vercel은 `frontend/vercel.json`으로 `/api/*` 요청을 Render 백엔드로 프록시한다.
