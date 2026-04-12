# AI MSP Intelligence Platform

## 목적
AI MSP 사업 전략팀용 경쟁사 동향 모니터링 플랫폼.
LG CNS, SK AX, 베스핀글로벌, PwC의 AI/AI Agent/ITO 관련 뉴스를
자동 수집·요약·분석하여 전략적 인사이트를 제공한다.

## 비즈니스 컨텍스트
- 타겟: 한국 금융·공공 엔터프라이즈
- 서비스: AI MSP (구독 + 성과 기반 혼합)
- 핵심 차별화: AI Agent 기반 ITO 전환, Vertical Agent IP

## 코딩 규칙
- Java 17, Spring Boot 3.2, Lombok 필수
- TypeScript strict mode, 함수형 컴포넌트만 사용
- API: RESTful JSON, 한국어 주석 허용
- 에러: GlobalExceptionHandler 전역 처리
- 크롤링: robots.txt 준수, 요청간격은 Manual로 처리, 이후 안정화 판단되면 1일 1회로 변경

## 환경 변수
CLAUDE_API_KEY는 docker-compose.yml에서 참고해서 사용할 것
Claude API는 유료버전이라서 Gemini-flash 2.5로 변경 (4/12)
GEMINI_API_KEY는 docker-compose.yml에서 참고해서 사용할 것
CRAWL_INTERVAL_HOURS은 Manual로 수행
INSIGHT_CRON도 Manual로 수행 
## =0 0 7 * * ?
DB_URL=jdbc:h2:mem:aimspdb

## Hosting Github Hosting 활용
프로잭트를 인터넷에 올리는 경우는 Github pages를 활용한다
주요 정보 노출 방지 위해 .env에 저장
https://github.com/kongdabu/ai-msp-intelligence