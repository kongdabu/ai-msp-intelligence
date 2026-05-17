package com.aimsp.intelligence.domain.weeklyreport;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class WeeklyReportSummary {

    private Long id;
    private String title;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Integer articleCount;
    private Integer insightCount;
    private boolean hasContent;
    private LocalDateTime generatedAt;
    private String downloadUrl;

    public static WeeklyReportSummary from(WeeklyReport r) {
        return WeeklyReportSummary.builder()
                .id(r.getId())
                .title(r.getTitle())
                .weekStart(r.getWeekStart())
                .weekEnd(r.getWeekEnd())
                .articleCount(r.getArticleCount())
                .insightCount(r.getInsightCount())
                .hasContent(r.getDocxContent() != null)
                .generatedAt(r.getGeneratedAt())
                .downloadUrl("/api/weekly-reports/" + r.getId() + "/download")
                .build();
    }
}
