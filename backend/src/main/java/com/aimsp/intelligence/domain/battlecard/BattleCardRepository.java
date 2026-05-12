package com.aimsp.intelligence.domain.battlecard;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleCardRepository extends JpaRepository<BattleCard, Long> {

    // N+1 방지: sourceArticles JOIN FETCH — 경쟁사별 최신 1건
    @EntityGraph(attributePaths = {"sourceArticles"})
    Optional<BattleCard> findTopByCompetitorOrderByGeneratedAtDesc(String competitor);

    // 이력 조회: 최근 10건 제한 + sourceArticles JOIN FETCH
    @EntityGraph(attributePaths = {"sourceArticles"})
    List<BattleCard> findTop10ByCompetitorOrderByGeneratedAtDesc(String competitor);

    // 단건 상세 조회: sourceArticles + article 모두 JOIN FETCH (명명 규칙 대신 @Query 사용)
    @Query("SELECT bc FROM BattleCard bc " +
           "LEFT JOIN FETCH bc.sourceArticles bca " +
           "LEFT JOIN FETCH bca.article " +
           "WHERE bc.id = :id")
    Optional<BattleCard> findByIdWithArticles(@Param("id") Long id);
}
