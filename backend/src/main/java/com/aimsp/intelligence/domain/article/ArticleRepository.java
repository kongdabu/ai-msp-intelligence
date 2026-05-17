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

    @Query("SELECT COUNT(a) FROM Article a WHERE a.collectedAt >= :startOfDay")
    long countTodayArticles(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT a.competitor, COUNT(a) FROM Article a GROUP BY a.competitor")
    List<Object[]> countByCompetitor();

    @Query("""
        SELECT a.category, CAST(a.publishedAt AS date), COUNT(a)
        FROM Article a
        WHERE a.publishedAt >= :since
        GROUP BY a.category, CAST(a.publishedAt AS date)
        ORDER BY CAST(a.publishedAt AS date)
    """)
    List<Object[]> countByCategoryAndDate(@Param("since") LocalDateTime since);

    List<Article> findByIsProcessedFalseOrderByCollectedAtDesc();

    Page<Article> findByCompetitorOrderByPublishedAtDesc(String competitor, Pageable pageable);

    List<Article> findTop5ByOrderByPublishedAtDesc();

    @Query("SELECT a FROM Article a WHERE a.publishedAt >= :from AND a.publishedAt < :to ORDER BY a.publishedAt DESC")
    List<Article> findByPublishedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
