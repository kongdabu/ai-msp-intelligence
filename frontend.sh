#!/bin/bash
# AI MSP Intelligence 프론트엔드 관리 스크립트
# 사용법: ./frontend.sh [start|stop|restart|status]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
LOG_FILE="/tmp/aimsp-frontend.log"
PID_FILE="/tmp/aimsp-frontend.pid"
PORT=3000

_check_running() {
    lsof -ti:$PORT > /dev/null 2>&1
}

_start() {
    if _check_running; then
        echo "이미 실행 중입니다. (port $PORT)"
        return 1
    fi

    echo "▶ 프론트엔드 시작 (http://localhost:$PORT)"
    cd "$FRONTEND_DIR"
    nohup npm run dev > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "  PID: $(cat $PID_FILE) | 로그: $LOG_FILE"

    # 기동 대기
    echo -n "  기동 중"
    for i in $(seq 1 15); do
        sleep 1
        if grep -q "Local:" "$LOG_FILE" 2>/dev/null; then
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

    echo "■ 프론트엔드 중지 (port $PORT)"
    lsof -ti:$PORT | xargs kill -9 2>/dev/null
    rm -f "$PID_FILE"
    echo "  ✅ 중지 완료"
}

_status() {
    if _check_running; then
        PID=$(lsof -ti:$PORT)
        echo "● 프론트엔드 실행 중 (PID: $PID, port: $PORT)"
        echo "  URL: http://localhost:$PORT"
    else
        echo "○ 프론트엔드 중지됨"
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
