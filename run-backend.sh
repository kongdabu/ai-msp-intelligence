#!/bin/bash
# AI MSP Intelligence 백엔드 실행 스크립트
# 사용법: ./run-backend.sh [GEMINI_API_KEY]
# 예시:   ./run-backend.sh AIzaSy...

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
GRADLE="/tmp/gradle-8.13/bin/gradle"

# Java 21 경로 설정
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH=$JAVA_HOME/bin:$PATH

# API 키 설정 (인자 > 환경변수 > .env 파일 순서)
if [ -n "$1" ]; then
    export GEMINI_API_KEY="$1"
elif [ -z "$GEMINI_API_KEY" ] && [ -f "$SCRIPT_DIR/.env" ]; then
    export $(grep -v '^#' "$SCRIPT_DIR/.env" | xargs)
fi

if [ -z "$GEMINI_API_KEY" ]; then
    echo "⚠️  GEMINI_API_KEY가 설정되지 않았습니다."
    echo "   AI 요약/인사이트 기능이 비활성화됩니다."
    echo "   설정 방법: ./run-backend.sh AIzaSy..."
fi

echo "▶ 백엔드 시작 (http://localhost:8080)"
echo "  API KEY: ${GEMINI_API_KEY:0:20}..."

cd "$BACKEND_DIR"
$GRADLE bootRun --no-daemon -Dapp.gemini.api-key="$GEMINI_API_KEY"
