package com.aimsp.intelligence.domain.weeklyreport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    @Transactional(readOnly = true)
    public List<WeeklyReportSummary> getRecentReportSummaries() {
        return weeklyReportRepository.findTop10ByOrderByGeneratedAtDesc()
                .stream()
                .map(WeeklyReportSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WeeklyReport getReport(Long id) {
        return weeklyReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레포트를 찾을 수 없습니다: " + id));
    }

    @Transactional
    public WeeklyReport saveUploadedReport(String title, String weekStart, String weekEnd,
                                           int articleCount, int insightCount, byte[] docxContent) {
        WeeklyReport report = new WeeklyReport();
        report.setTitle(title);
        report.setWeekStart(LocalDate.parse(weekStart));
        report.setWeekEnd(LocalDate.parse(weekEnd));
        report.setArticleCount(articleCount);
        report.setInsightCount(insightCount);
        report.setDocxContent(docxContent);
        report.setGeneratedAt(LocalDateTime.now());
        return weeklyReportRepository.save(report);
    }
}
