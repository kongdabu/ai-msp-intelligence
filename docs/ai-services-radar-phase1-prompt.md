# AI Services Radar Phase 1 구현 프롬프트

아래는 이번 구현에 사용한 Codex 작업 프롬프트다.

> 현재 프로젝트를 분석한 뒤 기존 기술 스택과 코드 스타일을 유지하면서 AI Services Radar 저장 기능(Phase 1)을 구현하라. 먼저 기존 아키텍처, 데이터베이스와 Flyway 마이그레이션 방식, API 응답 형식, 유효성 검증·예외 처리 패턴, 테스트 방식을 점검하고 짧은 구현 계획을 제시하라.
>
> 기존 `RadarSignal`은 내부 수집용 모델이므로 호환성을 보존한다. 외부 AI Services Radar 산출물은 별도 `RadarReport` 모델로 저장하고, 보고서와 Signal은 1:N, Signal과 Source는 1:N 관계로 구성한다. 클래스나 테이블 이름은 기존 모델과 충돌하지 않게 정한다.
>
> `RadarReport`는 `reportDate`, `reportType`, `title`, `executiveView`, `strategicInterpretation`, 원본 `markdown`, `promptVersion`, 생성·수정 시각을 보관한다. `RadarSignal` 개념에는 `company`, `category`, `importance`, `signal`, `fact`, `whatChanged`, `industryImpact`, `opportunity`, `threat`, `structuralRisk`, `practicalImplication`, `recommendedAction`을 둔다. `RadarSource`에는 `publisher`, `title`, `url`, `publishedDate`, `sourceType`을 둔다.
>
> `reportType + reportDate`의 DB 고유 제약을 추가하고 `POST /api/v1/radar/reports`가 같은 키로 다시 호출됐을 때 중복 삽입이 아니라 전체 구조화 데이터와 Markdown을 원자적으로 갱신하도록 구현하라. 다음 조회 API도 제공하라: `GET /api/v1/radar/reports/{date}`, `GET /api/v1/radar/reports`, `GET /api/v1/radar/signals`. 보고서에는 유형과 기간 필터·페이지네이션을, 신호에는 유형·기간·기업·카테고리·중요도·검색어 필터·페이지네이션을 제공하라.
>
> 기존 DTO 검증, 전역 예외 처리, 변경 API 토큰 보호, 서비스 트랜잭션, Flyway, 페이지 응답 규칙을 따른다. DTO/schema 검증과 서비스 단위 테스트를 추가한다. 관련 없는 리팩터링은 하지 말고, 구현 후 테스트를 실행한 뒤 변경 파일, API/스키마, 테스트 결과, 운영 마이그레이션 절차를 보고하라.
