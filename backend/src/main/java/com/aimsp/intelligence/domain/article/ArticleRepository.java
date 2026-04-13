package com.aimsp.intelligence.domain.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    Optional<Article> findByUrl(String url);

    boolean existsByUrl(String url);

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

    // 최신 기사 N건 (발행일 기준)
    List<Article> findTop5ByOrderByPublishedAtDesc();
}
