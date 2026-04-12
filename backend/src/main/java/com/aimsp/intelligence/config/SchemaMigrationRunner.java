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
                WHERE table_name = ? AND column_name = ?
                """;
            String dataType = jdbcTemplate.queryForObject(checkSql, String.class, table, column);

            if ("bytea".equals(dataType)) {
                String alterSql = String.format(
                    "ALTER TABLE %s ALTER COLUMN %s TYPE %s USING convert_from(%s, 'UTF8')",
                    table, column, targetType, column
                );
                jdbcTemplate.execute(alterSql);
                log.info("컬럼 타입 변환 완료: {}.{} bytea → {}", table, column, targetType);
            }
        } catch (Exception e) {
            log.warn("컬럼 확인/변환 실패 [{}.{}]: {}", table, column, e.getMessage());
        }
    }
}
