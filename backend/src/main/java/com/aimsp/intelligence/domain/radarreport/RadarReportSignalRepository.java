package com.aimsp.intelligence.domain.radarreport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RadarReportSignalRepository extends JpaRepository<RadarReportSignal, Long>, JpaSpecificationExecutor<RadarReportSignal> {
}
