package com.aimsp.intelligence.domain.insight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {

    // 타입/경쟁사 필터 조회
    @Query("""
        SELECT i FROM Insight i WHERE
        (:insightType IS NULL OR i.insightType = :insightType) AND
        (:competitor IS NULL OR i.competitor = :competitor)
        ORDER BY i.generatedAt DESC
    """)
    Page<Insight> findWithFilters(
            @Param("insightType") String insightType,
            @Param("competitor") String competitor,
            Pageable pageable
    );

    // 오늘 생성된 인사이트
    List<Insight> findByGeneratedAtAfterOrderByImpactScoreDesc(LocalDateTime since);

    // 고영향도 인사이트 수
    long countByImpactScoreGreaterThanEqual(Integer minScore);

    // 최근 인사이트 N건 (대시보드용)
    List<Insight> findTop3ByOrderByGeneratedAtDesc();
}
