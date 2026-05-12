package com.aimsp.intelligence.domain.battlecard;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 단건 상세 조회: sourceArticles + 각 article JOIN FETCH
    @EntityGraph(attributePaths = {"sourceArticles", "sourceArticles.article"})
    Optional<BattleCard> findWithDetailById(Long id);
}
