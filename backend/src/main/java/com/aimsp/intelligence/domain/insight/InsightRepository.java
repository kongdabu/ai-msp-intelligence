package com.aimsp.intelligence.domain.insight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long>, JpaSpecificationExecutor<Insight> {

    // 오늘 생성된 인사이트
    List<Insight> findByGeneratedAtAfterOrderByImpactScoreDesc(LocalDateTime since);

    // 특정 시점 이후 인사이트 수 (대시보드 KPI용 - 전체 로딩 없이 COUNT 쿼리)
    long countByGeneratedAtAfter(LocalDateTime since);

    // 고영향도 인사이트 수
    long countByImpactScoreGreaterThanEqual(Integer minScore);

    // 최근 인사이트 N건 (대시보드용)
    List<Insight> findTop3ByOrderByGeneratedAtDesc();
}
