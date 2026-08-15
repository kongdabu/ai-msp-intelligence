package com.aimsp.intelligence.config;

import org.slf4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** 작업 실행 시작 로그의 형식을 통일한다. */
public final class TaskExecutionLogger {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    private TaskExecutionLogger() {
    }

    /**
     * 기존 로그와 구분되도록 세 줄을 띄운 뒤 작업 시작 정보를 남긴다.
     */
    public static void logStart(Logger log, String taskName) {
        String executedAt = ZonedDateTime.now(SEOUL_ZONE_ID).format(DATE_TIME_FORMATTER);
        log.info("\\n\\n\\n\\n========== 작업 시작 ==========\\n작업명: {}\\n실행 일시: {}\\n===============================", taskName, executedAt);
    }
}
