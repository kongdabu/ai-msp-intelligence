package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.trend.TrendNews;
import com.aimsp.intelligence.domain.trend.TrendNewsArticle;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrendNewsDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private String title;
        private String summary;
        private Integer trendScore;
        private Integer confidenceScore;
        private String status;
        private List<String> keywords;
        private int sourceArticleCount;
        private LocalDateTime generatedAt;

        public static Response from(TrendNews trendNews) {
            return Response.builder()
                    .id(trendNews.getId())
                    .periodStart(trendNews.getPeriodStart())
                    .periodEnd(trendNews.getPeriodEnd())
                    .title(trendNews.getTitle())
                    .summary(trendNews.getSummary())
                    .trendScore(trendNews.getTrendScore())
                    .confidenceScore(trendNews.getConfidenceScore())
                    .status(trendNews.getStatus())
                    .keywords(new ArrayList<>(trendNews.getKeywords()))
                    .sourceArticleCount(trendNews.getSourceArticleCount())
                    .generatedAt(trendNews.getGeneratedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SourceArticleResponse {
        private Long id;
        private String title;
        private String url;
        private String sourceName;
        private String competitor;
        private String category;
        private String summary;
        private LocalDateTime publishedAt;
        private Integer relevanceScore;

        public static SourceArticleResponse from(TrendNewsArticle association) {
            var article = association.getArticle();
            return SourceArticleResponse.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .url(article.getUrl())
                    .sourceName(article.getSourceName())
                    .competitor(article.getCompetitor())
                    .category(article.getCategory())
                    .summary(article.getSummary())
                    .publishedAt(article.getPublishedAt())
                    .relevanceScore(association.getRelevanceScore())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private String title;
        private String summary;
        private String content;
        private Integer trendScore;
        private Integer confidenceScore;
        private String status;
        private List<String> keywords;
        private List<String> actionItems;
        private List<SourceArticleResponse> sourceArticles;
        private LocalDateTime generatedAt;

        public static DetailResponse from(TrendNews trendNews) {
            List<SourceArticleResponse> sourceArticles = trendNews.getSourceArticles().stream()
                    .map(SourceArticleResponse::from)
                    .collect(Collectors.toList());
            return DetailResponse.builder()
                    .id(trendNews.getId())
                    .periodStart(trendNews.getPeriodStart())
                    .periodEnd(trendNews.getPeriodEnd())
                    .title(trendNews.getTitle())
                    .summary(trendNews.getSummary())
                    .content(trendNews.getContent())
                    .trendScore(trendNews.getTrendScore())
                    .confidenceScore(trendNews.getConfidenceScore())
                    .status(trendNews.getStatus())
                    .keywords(new ArrayList<>(trendNews.getKeywords()))
                    .actionItems(new ArrayList<>(trendNews.getActionItems()))
                    .sourceArticles(sourceArticles)
                    .generatedAt(trendNews.getGeneratedAt())
                    .build();
        }
    }
}
