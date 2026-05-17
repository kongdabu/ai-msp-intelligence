package com.aimsp.intelligence.domain.weeklyreport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    @Transactional(readOnly = true)
    public List<WeeklyReport> getRecentReports() {
        return weeklyReportRepository.findTop10ByOrderByGeneratedAtDesc();
    }

    @Transactional(readOnly = true)
    public WeeklyReport getReport(Long id) {
        return weeklyReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레포트를 찾을 수 없습니다: " + id));
    }
}
