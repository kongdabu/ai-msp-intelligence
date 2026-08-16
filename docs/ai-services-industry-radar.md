# AI Services Industry Radar

## 목적

이 기능은 AI 뉴스의 건수를 보여주는 화면이 아니다. 신뢰할 수 있는 원문에서 확인한 변화 신호를 산업 구조와 한국 AI MSP의 실행 과제로 연결한다.

흐름은 다음과 같다.

`원문 근거 → Signal → 6개 Lens → 사업 구조 영향 → 주간 브리핑`

## 데이터 기준

`RadarPlayer`는 감시 대상 사업자다. Frontier Lab, CSP·플랫폼, 컨설팅, 글로벌 SI·MSP, 국내 SI·MSP의 다섯 계층으로 관리한다. 초기 Watch List는 35개 사업자이며, 수집 건수가 아니라 사업 구조에 미치는 중요도를 기준으로 우선순위를 부여한다.

`RadarSignal`은 하나의 검증된 변화다. 출처 URL은 중복 등록할 수 없으며 다음 내용을 모두 가져야 한다.

- 사실 요약과 발생 일시
- 출처 등급, 신뢰도, 영향도
- 관련 플레이어와 6개 Lens 중 하나 이상
- 무엇이 바뀌었는지, 산업 구조 영향, 기회·위협·리스크, 권고 행동

## 6개 Lens

| 코드 | 관점 |
| --- | --- |
| `AI_AGENT` | 에이전트 제품·플랫폼과 자율 업무 실행 구조 |
| `FRONTIER_LABS` | 모델 경쟁력과 생태계 지배력 |
| `PARTNERSHIP` | 모델사·클라우드·SI의 결합 및 판매 구조 |
| `DEPLOYMENT_MODEL` | FDE·RDE·ODE 등 현장 투입형 AI 딜리버리 |
| `AI_PRICING` | 인력 투입형에서 사용량·성과형으로의 전환, 토큰 단가 인하 및 비용 체계 개편 |
| `AGENTIC_OPERATIONS` | AIOps·운영 자동화와 관리형 서비스 재편 |

## API

- `GET /api/radar/overview`: Watch List, 최근 신호, 6개 Lens, 주간 브리핑을 한 번에 조회한다.
- `GET /api/radar/signals`: 최근 검증 신호를 조회한다. (params: lens, minimumImpactScore, page, size)
- `POST /api/radar/signals`: 검증된 신호를 등록한다. `API_SECRET_TOKEN`을 설정한 환경에서는 `X-API-Token`이 필요하다.
- `GET /api/radar/players`: Watch List 감시 대상 사업자 목록을 조회한다.
- `PUT /api/radar/players/{id}`: Watch List 사업자의 활성 여부, 우선순위, 웹사이트를 수정한다.
- `POST /api/radar/collect`: 공식 사이트·활성 수집 소스에서 새 원문을 수집하고, Gemini로 Radar Signal 적합성을 검증한 뒤 자동 등록한다. 작업은 백그라운드로 실행된다.
- `GET /api/radar/collect/status`: Radar 수집 작업의 진행 상태와 수집·분석·등록 건수를 조회한다.
- `POST /api/radar/collect/cancel`: 실행 중인 Radar 수집 작업을 취소한다.
- `POST /api/radar/weekly-briefs/generate`: 최근 7일 신호를 근거로 주간 브리핑을 만든다. 신호가 없으면 만들지 않는다.

## 운영 원칙

출처의 수나 수집 건수로 품질을 판단하지 않는다. 자동 수집기는 새 원문을 최대 12건까지 분석하며, 원문 근거, 관련 플레이어, 6개 Lens 분류, 구조적 영향과 권고 행동이 모두 있는 신호만 저장하고 주간 브리핑에 포함한다.

원문 URL은 Signal 등록 시 확인한다. 이후에도 최근 Signal의 URL을 매시간 재확인해 `404` 또는 `410`을 반환한 원문은 `SOURCE_UNAVAILABLE` 상태로 바꾸고 Radar 화면과 주간 브리핑에서 자동 제외한다. `403`, `429`, `5xx`, 네트워크 오류는 접근 제한 또는 일시 오류일 수 있으므로 자동 제외하지 않는다.

공식 사이트의 후보 발견은 목록 페이지 하나에 의존하지 않는다. 각 소스에서 목록 링크, RSS·Atom 피드, robots.txt에 선언된 사이트맵을 모두 독립적으로 조회해 URL을 합친 뒤 중복을 제거한다. 목록 페이지가 동적으로 렌더링되거나 일부 항목만 노출돼도 동일한 관심 주제 조건으로 피드·사이트맵 후보를 계속 검토한다.
