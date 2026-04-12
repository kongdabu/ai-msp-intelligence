#!/bin/bash
# AI MSP Intelligence 백엔드 관리 스크립트
# 사용법: ./backend.sh [start|stop|restart|status]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
GRADLE="/tmp/gradle-8.13/bin/gradle"
LOG_FILE="/tmp/aimsp-backend.log"
PID_FILE="/tmp/aimsp-backend.pid"
PORT=8080

export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH=$JAVA_HOME/bin:$PATH

# .env 로드
if [ -f "$SCRIPT_DIR/.env" ]; then
    export $(grep -v '^#' "$SCRIPT_DIR/.env" | xargs)
fi

_check_running() {
    lsof -ti:$PORT > /dev/null 2>&1
}

_start() {
    if _check_running; then
        echo "이미 실행 중입니다. (port $PORT)"
        return 1
    fi

    if [ -z "$GEMINI_API_KEY" ]; then
        echo "⚠️  GEMINI_API_KEY가 설정되지 않았습니다. (.env 파일 확인)"
    fi

    echo "▶ 백엔드 시작 (http://localhost:$PORT)"
    cd "$BACKEND_DIR"
    nohup $GRADLE bootRun --no-daemon -Dapp.gemini.api-key="$GEMINI_API_KEY" \
        > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"

    echo "  PID: $(cat $PID_FILE) | 로그: $LOG_FILE"

    # 기동 대기
    echo -n "  기동 중"
    for i in $(seq 1 30); do
        sleep 2
        if grep -q "Started AiMspApplication" "$LOG_FILE" 2>/dev/null; then
            echo " ✅ 완료"
            return 0
        fi
        echo -n "."
    done
    echo " ⚠️  타임아웃 (로그 확인: tail -f $LOG_FILE)"
}

_stop() {
    if ! _check_running; then
        echo "실행 중이지 않습니다."
        return 0
    fi

    echo "■ 백엔드 중지 (port $PORT)"
    lsof -ti:$PORT | xargs kill -9 2>/dev/null
    rm -f "$PID_FILE"
    echo "  ✅ 중지 완료"
}

_status() {
    if _check_running; then
        PID=$(lsof -ti:$PORT)
        echo "● 백엔드 실행 중 (PID: $PID, port: $PORT)"
        curl -s http://localhost:$PORT/actuator/health 2>/dev/null && echo ""
    else
        echo "○ 백엔드 중지됨"
    fi
}

case "$1" in
    start)   _start ;;
    stop)    _stop ;;
    restart) _stop && sleep 1 && _start ;;
    status)  _status ;;
    *)
        echo "사용법: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
