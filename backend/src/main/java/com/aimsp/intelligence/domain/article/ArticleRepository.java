package com.aimsp.intelligence.domain.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findByUrl(String url);

    boolean existsByUrl(String url);

    // 필터 조건 검색
    @Query("""
        SELECT a FROM Article a WHERE
        (:competitor IS NULL OR a.competitor = :competitor) AND
        (:category IS NULL OR a.category = :category) AND
        (:sourceType IS NULL OR a.sourceType = :sourceType) AND
        (:keyword IS NULL OR a.title LIKE CONCAT('%', :keyword, '%')
            OR a.summary LIKE CONCAT('%', :keyword, '%')) AND
        (:dateFrom IS NULL OR a.publishedAt >= :dateFrom) AND
        (:dateTo IS NULL OR a.publishedAt <= :dateTo)
        ORDER BY a.collectedAt DESC
    """)
    Page<Article> findWithFilters(
            @Param("competitor") String competitor,
            @Param("category") String category,
            @Param("sourceType") String sourceType,
            @Param("keyword") String keyword,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    // 오늘 수집된 기사 수
    @Query("SELECT COUNT(a) FROM Article a WHERE a.collectedAt >= :startOfDay")
    long countTodayArticles(@Param("startOfDay") LocalDateTime startOfDay);

    // 경쟁사별 기사 수
    @Query("SELECT a.competitor, COUNT(a) FROM Article a GROUP BY a.competitor")
    List<Object[]> countByCompetitor();

    // 카테고리별 기사 수 (날짜 범위)
    @Query("""
        SELECT a.category, CAST(a.publishedAt AS date), COUNT(a)
        FROM Article a
        WHERE a.publishedAt >= :since
        GROUP BY a.category, CAST(a.publishedAt AS date)
        ORDER BY CAST(a.publishedAt AS date)
    """)
    List<Object[]> countByCategoryAndDate(@Param("since") LocalDateTime since);

    // 미처리 기사 목록
    List<Article> findByIsProcessedFalseOrderByCollectedAtDesc();

    // 특정 경쟁사 최근 기사
    Page<Article> findByCompetitorOrderByPublishedAtDesc(String competitor, Pageable pageable);

    // 최신 기사 N건
    List<Article> findTop5ByOrderByCollectedAtDesc();
}
