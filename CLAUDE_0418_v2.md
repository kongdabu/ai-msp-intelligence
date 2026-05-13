# 2026-04-18 작업 내역 (v2)

## 1. 중단된 작업 이어받기

### 크롤러 OkHttp 마이그레이션 (커밋: e1175d1)
- `BespinCrawler`, `LgCnsCrawler`, `PwcCrawler`, `SkAxCrawler`, `ZdnetKoreaCrawler`
- `XmlReader(new URL(...))` → OkHttp `fetchRaw()` + `StringReader` 방식으로 변경
- CLAUDE.md 하단 비정형 메모 정리

---

## 2. Google News RSS 503 오류 대응

### 원인 분석
- Google News RSS가 서버사이드 접근을 봇으로 감지 → 503 반환
- 5개 크롤러가 병렬 실행 시 동시 요청으로 Rate Limit 발생
- `RSSReader/1.0` User-Agent 봇 식별
- 200 응답이나 `content-length: 0` (빈 바디) — DotsSplashUi 챌린지 페이지

### 시도한 수정들
1. **Chrome UA 변경 + 빈 바디 감지** (커밋: 0feff3d)
2. **랜덤 지터(0~8s) + 재시도 10s/20s + 스레드풀 3→1** (커밋: 89a5810)
3. 근본 해결 불가 판단 → **Naver 뉴스 검색 API로 전환**

---

## 3. Naver 뉴스 검색 API 전환 (커밋: 5c39f78)

### 변경 내용
- `NaverNewsClient.java` 신규 생성 (`crawler/` 패키지)
  - `X-Naver-Client-Id` / `X-Naver-Client-Secret` 헤더 인증
  - `display=20`, `sort=date` 파라미터
  - HTML 태그/엔티티 제거 (`cleanTitle()`, `cleanDescription()`)
  - RFC 1123 날짜 파싱 → KST `LocalDateTime`
  - 원본 URL 우선 반환 (`bestUrl()`: `originallink` > `link`)
  - Naver 키 미설정 시 graceful skip (로컬 개발 대응)
- 5개 소스 크롤러 교체 (Google News RSS → Naver API)
  - `LG CNS` / `SK AX` / `베스핀글로벌` / `삼일PwC` / `AI MSP`
- `GoogleNewsRssFetcher.java` 삭제
- `CrawlerOrchestrator` 스레드풀 복원 (1→3)
- `AppConfig.java` + `application.yml` Naver 설정 추가

### 환경변수 추가 (Render)
```
NAVER_CLIENT_ID     = aZrLf11k49IAvQ4HHEXq
NAVER_CLIENT_SECRET = D3SCc6EzeG
```
- Naver Developers 앱 등록 → 사용 API: **검색**

---

## 4. DB Sources Google News 비활성화 (커밋: 4ff9512)

### 원인
- `RssCrawler`(DB Source용)도 Google News RSS URL 사용 → 동일 503 오류

### 변경 내용
- `data.sql`: Google News 3개 소스 `active = false`로 변경
- `data-prod.sql`: `ON CONFLICT DO UPDATE SET active = false` 추가
- `ZdnetKoreaCrawler` 다중 쿼리 확장 (URL 중복 제거 포함):
  - "AI MSP", "AI 에이전트", "클라우드 MSP", "금융 AI", "AI ITO"
- **Prod DB 조치**: Supabase에서 Google News 3개 소스 비활성화 완료

---

## 5. 보안 취약점 점검 및 수정 (커밋: a0a64be)

### 점검 결과 요약
| 항목 | 심각도 | 상태 |
|------|--------|------|
| API 키 노출 (.env) | CRITICAL | .gitignore 포함 → Git 미커밋 (안전) |
| 인증 부재 (crawl/generate) | HIGH | 미수정 (내부 전용 도구로 판단 보류) |
| SSRF (소스 URL 등록) | MEDIUM | **수정 완료** |
| CORS 와일드카드 헤더 | MEDIUM | **수정 완료** |
| H2 콘솔 (로컬만) | MEDIUM | prod 비활성화 확인됨 |
| SQL Injection | LOW | JPA 파라미터화 쿼리로 안전 |
| XSS (프론트) | SAFE | dangerouslySetInnerHTML 미사용 |
| 프론트 API 키 하드코딩 | SAFE | 상대경로 + Vercel 프록시 사용 |

### 수정 내용
- **CorsConfig.java**: `allowedHeaders("*")` → 명시적 헤더 화이트리스트
  ```java
  .allowedHeaders("Content-Type", "Accept", "Authorization", "X-Requested-With")
  ```
- **SourceController.java**: SSRF 방어 `validateUrl()` 추가
  - `http/https` 스키마만 허용
  - localhost, 127.x, 10.x, 192.168.x, 172.16~31.x, 169.254.x 등 내부망 차단
  - 잘못된 URL 시 400 Bad Request 반환

---

## 6. 반응형 웹 구현 (커밋: a0a64be)

### 분석 결과 (우선순위 기준)
| 순위 | 파일 | 문제 | 심각도 |
|------|------|------|--------|
| 1 | Sidebar | `w-60` 고정 → 모바일 화면 압도 | 🔴 심각 |
| 2 | Articles | 슬라이드 패널 `w-96` → 모바일 초과 | 🔴 심각 |
| 3 | Sources | 7열 테이블 → 가로 스크롤 불가 | 🔴 심각 |
| 4 | Dashboard | KPI/차트 그리드 태블릿 미지원 | 🟠 높음 |
| 5 | Competitors | `lg:` 전용 → 태블릿 레이아웃 깨짐 | 🟠 높음 |
| 6 | Insights | 탭 5개 → 모바일 overflow | 🟠 높음 |
| 7 | 전체 페이지 | `p-6` 고정 → 모바일 과도 | 🟡 중간 |
| 8 | Header | flex-row 강제 → 모바일 겹침 | 🟡 중간 |

### 구현 내용

#### App.tsx
- `sidebarOpen` state 추가
- 모바일 오버레이 (`fixed inset-0 bg-black/50 z-20 md:hidden`)

#### Sidebar.tsx
- `fixed md:static` + `translate-x-full md:translate-x-0` 슬라이드 처리
- 모바일 X 닫기 버튼, NavLink 클릭 시 `onClose()` 호출

#### Header.tsx
- 햄버거 버튼 (`Menu` 아이콘, `md:hidden`)
- 반응형 패딩 `px-4 sm:px-6`, 날짜 `hidden sm:block`

#### Dashboard.tsx
- 패딩: `p-4 sm:p-6 space-y-4 sm:space-y-6`
- 차트 그리드: `lg:grid-cols-2` → `md:grid-cols-2`
- 인사이트+기사: `lg:grid-cols-3` → `md:grid-cols-3`
- 인사이트 내부: `md:grid-cols-2` → `sm:grid-cols-2`

#### Articles.tsx
- 패널 로직 분리: `ArticleDetail` 컴포넌트 추출
- 모바일: `md:hidden fixed inset-0 bg-white z-40` (전체화면)
- 데스크탑: `hidden md:block fixed right-0 w-96` (슬라이드)

#### Insights.tsx
- 탭: `overflow-x-auto` + `w-max` + `whitespace-nowrap`
- 버튼: `px-3 sm:px-4`, `text-xs sm:text-sm`
- 그리드: `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3`

#### Competitors.tsx
- 탭: `overflow-x-auto` + `px-3 sm:px-5`, `text-xs sm:text-sm`
- 그리드: `lg:grid-cols-3` → `md:grid-cols-3`
- col-span: `lg:col-span-2` → `md:col-span-2`

#### Sources.tsx
- 테이블: `overflow-x-auto` 래퍼 + `min-w-[640px]`
- 소스 추가 폼: `grid-cols-1 sm:grid-cols-2 md:grid-cols-4`
- 버튼 텍스트: 모바일 `hidden sm:inline` 처리

---

## 커밋 히스토리

| 커밋 | 내용 |
|------|------|
| e1175d1 | Fix all source crawlers: use OkHttp fetchRaw instead of XmlReader(URL) |
| 0feff3d | Fix Google News bot detection: use full Chrome UA and handle empty body |
| 89a5810 | Reduce Google News 503: jitter delay + sequential crawling + longer retries |
| 5c39f78 | Replace Google News RSS with Naver News Search API |
| 4ff9512 | Disable Google News RSS sources; cover keywords via Naver API |
| a0a64be | Add responsive web support and security fixes |
