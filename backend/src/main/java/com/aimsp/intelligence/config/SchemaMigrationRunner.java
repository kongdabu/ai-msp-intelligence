package com.aimsp.intelligence.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL 운영 환경 전용 스키마 수정 Runner.
 * @Lob 제거 후 bytea 타입으로 남은 컬럼을 text/varchar로 변환한다.
 * Hibernate ddl-auto: update 이후 실행되므로 순환 의존성 없음.
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class SchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== PostgreSQL 스키마 마이그레이션 시작 ===");
        fixByteaColumn("article", "title", "varchar(500)");
        fixByteaColumn("article", "summary", "varchar(500)");
        fixByteaColumn("article", "original_content", "text");
        fixByteaColumn("article", "source_name", "varchar(200)");
        fixByteaColumn("insight", "title", "varchar(200)");
        fixByteaColumn("insight", "content", "text");
        log.info("=== PostgreSQL 스키마 마이그레이션 완료 ===");
    }

    private void fixByteaColumn(String table, String column, String targetType) {
        try {
            String checkSql = """
                SELECT data_type FROM information_schema.columns
                WHERE table_schema = current_schema() AND table_name = ? AND column_name = ?
                """;
            String dataType = jdbcTemplate.queryForObject(checkSql, String.class, table, column);

            if ("bytea".equals(dataType)) {
                // 1차 시도: convert_from (정상 UTF-8 데이터)
                try {
                    jdbcTemplate.execute(String.format(
                        "ALTER TABLE %s ALTER COLUMN %s TYPE %s USING convert_from(%s, 'UTF8')",
                        table, column, targetType, column
                    ));
                    log.info("컬럼 타입 변환 완료: {}.{} bytea → {}", table, column, targetType);
                } catch (Exception e1) {
                    log.warn("convert_from 실패, encode 방식으로 재시도 [{}.{}]: {}", table, column, e1.getMessage());
                    // 2차 시도: encode(escape) - 데이터 손실 없이 문자열로 변환
                    jdbcTemplate.execute(String.format(
                        "ALTER TABLE %s ALTER COLUMN %s TYPE %s USING encode(%s, 'escape')::%s",
                        table, column, targetType, column, targetType
                    ));
                    log.info("컬럼 타입 변환 완료(escape): {}.{} bytea → {}", table, column, targetType);
                }
            } else {
                log.info("컬럼 타입 정상 ({}) - 변환 불필요: {}.{}", dataType, table, column);
            }
        } catch (Exception e) {
            log.error("컬럼 확인/변환 실패 [{}.{}]: {}", table, column, e.getMessage());
        }
    }
}
