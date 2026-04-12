package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.domain.insight.InsightService;
import com.aimsp.intelligence.domain.source.SourceService;
import com.aimsp.intelligence.dto.DashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleService articleService;
    private final InsightService insightService;
    private final SourceService sourceService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardDto> getSummary() {
        // KPI
        Map<String, Object> stats = articleService.getStats();
        long todayCount = (long) stats.get("todayCount");
        long unprocessedInsightCount = insightService.getUnprocessedInsightCount();
        long highImpactCount = insightService.getHighImpactCount();
        long activeSourceCount = sourceService.getActiveSourceCount();

        // 경쟁사별 분포
        @SuppressWarnings("unchecked")
        Map<String, Long> competitorDist = (Map<String, Long>) stats.get("byCompetitor");

        // 카테고리 트렌드 파싱
        @SuppressWarnings("unchecked")
        List<Object[]> rawTrend = (List<Object[]>) stats.get("categoryTrend");
        List<DashboardDto.CategoryTrend> trends = new ArrayList<>();
        if (rawTrend != null) {
            trends = rawTrend.stream()
                    .map(row -> DashboardDto.CategoryTrend.builder()
                            .category(row[0] != null ? row[0].toString() : "")
                            .date(row[1] != null ? row[1].toString() : "")
                            .count(row[2] != null ? Long.parseLong(row[2].toString()) : 0L)
                            .build())
                    .collect(Collectors.toList());
        }

        DashboardDto dashboard = DashboardDto.builder()
                .todayArticleCount(todayCount)
                .unprocessedInsightCount(unprocessedInsightCount)
                .highImpactInsightCount(highImpactCount)
                .activeSourceCount(activeSourceCount)
                .competitorDistribution(competitorDist)
                .categoryTrends(trends)
                .latestInsights(insightService.getLatestInsights())
                .latestArticles(articleService.getLatestArticles())
                .build();

        return ResponseEntity.ok(dashboard);
    }
}
