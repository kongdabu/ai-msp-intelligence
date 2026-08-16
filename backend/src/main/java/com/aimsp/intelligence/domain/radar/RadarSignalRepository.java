package com.aimsp.intelligence.domain.radar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.time.LocalDateTime;

public interface RadarSignalRepository extends JpaRepository<RadarSignal, Long>, JpaSpecificationExecutor<RadarSignal> {

    List<RadarSignal> findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(String status);

    long countByStatusNotAndImpactScoreGreaterThanEqual(String status, int impactScore);

    @Query("""
            select lens, count(distinct signal)
            from RadarSignal signal join signal.lenses lens
            where signal.status <> :excludedStatus
            group by lens
            """)
    List<Object[]> countByLensExcludingStatus(@Param("excludedStatus") String excludedStatus);

    Optional<RadarSignal> findBySourceUrl(String sourceUrl);

    List<RadarSignal> findBySourceUrlIn(Collection<String> sourceUrls);

    List<RadarSignal> findByOccurredAtBetweenOrderByImpactScoreDescOccurredAtDesc(LocalDateTime start, LocalDateTime end);

    List<RadarSignal> findTop20ByStatusNotOrderByCapturedAtDesc(String status);
}
