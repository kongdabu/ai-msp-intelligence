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
1) 주요 정보 노출 방지 위해 .env에 저장
2) https://github.com/kongdabu/ai-msp-intelligence

render host : dpg-d7dhpnho3t8c73d1tru0-a
3) Backend Service URL : https://aimsp-backend.onrender.com
4) Frontend Service URL : 

Vercel 배포 절차                                                                                                                                                      
  1. 프로젝트 생성                                                                              
  1. vercel.com 접속 → GitHub 로그인                                                       
  2. Add New Project → kongdabu/ai-msp-intelligence 선택                                   
  3. Root Directory → frontend 로 변경 (중요!)                                             
                                                                                           
  2. 빌드 설정 확인                                                                
  ┌──────────────────┬───────────────┐                                                     
  │       항목       │      값       │                                                     
  ├──────────────────┼───────────────┤                                                     
  │ Framework        │ Vite          │                                                     
  ├──────────────────┼───────────────┤
  │ Root Directory   │ frontend      │
  ├──────────────────┼───────────────┤
  │ Build Command    │ npm run build │
  ├──────────────────┼───────────────┤                                                     
  │ Output Directory │ dist          │
  └──────────────────┴───────────────┘                                                                                      
  3. Deploy 클릭

  - 별도 환경변수 설정 불필요                                                              
  - /api/* 요청은 자동으로 https://aimsp-backend.onrender.com/api/*로 프록시됨                    

    curl -X POST https://aimsp-backend.onrender.com/api/articles/crawl