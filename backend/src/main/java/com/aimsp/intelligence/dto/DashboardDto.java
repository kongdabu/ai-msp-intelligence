package com.aimsp.intelligence.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardDto {

    // KPI 지표
    private long todayArticleCount;
    private long unprocessedInsightCount;
    private long highImpactInsightCount;
    private long activeSourceCount;

    // 차트 데이터
    private Map<String, Long> competitorDistribution; // 경쟁사별 기사 수
    private List<CategoryTrend> categoryTrends;       // 7일 카테고리 트렌드

    // 최근 인사이트/기사
    private List<InsightDto.Response> latestInsights;
    private List<ArticleDto.Response> latestArticles;

    @Getter
    @Builder
    public static class CategoryTrend {
        private String date;
        private String category;
        private Long count;
    }
}
