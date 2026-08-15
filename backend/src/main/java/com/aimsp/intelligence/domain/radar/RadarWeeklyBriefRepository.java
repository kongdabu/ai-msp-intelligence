package com.aimsp.intelligence.domain.radar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RadarWeeklyBriefRepository extends JpaRepository<RadarWeeklyBrief, Long> {

    List<RadarWeeklyBrief> findTop8ByOrderByPeriodEndDesc();
}
