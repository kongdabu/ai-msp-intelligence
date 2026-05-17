package com.aimsp.intelligence.domain.weeklyreport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    List<WeeklyReport> findTop10ByOrderByGeneratedAtDesc();
}
