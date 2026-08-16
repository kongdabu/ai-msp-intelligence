package com.aimsp.intelligence.domain.radar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RadarPlayerRepository extends JpaRepository<RadarPlayer, Long> {

    List<RadarPlayer> findByActiveTrueOrderByLayerAscWatchPriorityAscNameAsc();

    boolean existsByName(String name);

    long countByActiveTrue();
}
