package com.aimsp.intelligence.domain.battlecard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BattleCardRepository extends JpaRepository<BattleCard, Long> {

    List<BattleCard> findByCompetitorOrderByGeneratedAtDesc(String competitor);

    Optional<BattleCard> findTopByCompetitorOrderByGeneratedAtDesc(String competitor);
}
