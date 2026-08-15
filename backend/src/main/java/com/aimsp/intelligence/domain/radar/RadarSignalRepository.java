package com.aimsp.intelligence.domain.radar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface RadarSignalRepository extends JpaRepository<RadarSignal, Long> {

    List<RadarSignal> findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(String status);

    long countByStatusNotAndImpactScoreGreaterThanEqual(String status, int impactScore);

    Optional<RadarSignal> findBySourceUrl(String sourceUrl);

    List<RadarSignal> findByOccurredAtBetweenOrderByImpactScoreDescOccurredAtDesc(LocalDateTime start, LocalDateTime end);

    List<RadarSignal> findTop20ByStatusNotOrderByCapturedAtDesc(String status);
}
