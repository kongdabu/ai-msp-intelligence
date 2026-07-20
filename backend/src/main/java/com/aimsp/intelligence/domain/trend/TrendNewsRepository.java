package com.aimsp.intelligence.domain.trend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrendNewsRepository extends JpaRepository<TrendNews, Long> {

    List<TrendNews> findTop20ByOrderByGeneratedAtDescTrendScoreDesc();

    @Query("SELECT tn FROM TrendNews tn " +
           "LEFT JOIN FETCH tn.sourceArticles tna " +
           "LEFT JOIN FETCH tna.article " +
           "WHERE tn.id = :id")
    Optional<TrendNews> findByIdWithArticles(@Param("id") Long id);
}
