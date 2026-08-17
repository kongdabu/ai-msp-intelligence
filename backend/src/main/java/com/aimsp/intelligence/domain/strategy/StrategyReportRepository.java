package com.aimsp.intelligence.domain.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StrategyReportRepository extends JpaRepository<StrategyReport, Long> {

    Page<StrategyReport> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    Optional<StrategyReport> findTopByOrderByGeneratedAtDesc();
}
