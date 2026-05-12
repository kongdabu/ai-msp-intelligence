#!/bin/bash
# AI MSP Intelligence 백엔드 실행 스크립트
# 사용법: ./run-backend.sh [GEMINI_API_KEY]
# 예시:   ./run-backend.sh AIzaSy...

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
GRADLE="./gradlew"

# .env 파일 로드 (인자로 GEMINI_API_KEY 전달 시 덮어쓰기)
if [ -f "$SCRIPT_DIR/.env" ]; then
    export $(grep -v '^#' "$SCRIPT_DIR/.env" | xargs)
fi
if [ -n "$1" ]; then
    export GEMINI_API_KEY="$1"
fi

if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  GEMINI_API_KEY가 설정되지 않았습니다."
    echo "   AI 요약/인사이트 기능이 비활성화됩니다."
fi

echo "▶ 백엔드 시작 (http://localhost:8080)"
echo "  GEMINI_API_KEY     : ${GEMINI_API_KEY:0:20}..."
echo "  NAVER_CLIENT_ID    : ${NAVER_CLIENT_ID:-(미설정)}"
echo "  PROCUREMENT_API_KEY: ${PROCUREMENT_API_KEY:-(미설정)}"
echo "  SARAMIN_API_KEY    : ${SARAMIN_API_KEY:-(미설정)}"

cd "$BACKEND_DIR"
$GRADLE bootRun --no-daemon \
  -Dapp.gemini.api-key="$GEMINI_API_KEY" \
  -Dapp.naver.client-id="$NAVER_CLIENT_ID" \
  -Dapp.naver.client-secret="$NAVER_CLIENT_SECRET" \
  -Dapp.procurement.api-key="$PROCUREMENT_API_KEY" \
  -Dapp.saramin.api-key="$SARAMIN_API_KEY"
