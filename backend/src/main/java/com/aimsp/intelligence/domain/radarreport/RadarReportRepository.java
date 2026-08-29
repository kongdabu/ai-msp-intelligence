package com.aimsp.intelligence.domain.radarreport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface RadarReportRepository extends JpaRepository<RadarReport, Long>, JpaSpecificationExecutor<RadarReport> {
    Optional<RadarReport> findByReportTypeAndReportDate(String reportType, LocalDate reportDate);
}
