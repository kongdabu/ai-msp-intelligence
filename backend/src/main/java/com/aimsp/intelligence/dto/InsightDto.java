package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.insight.Insight;
import com.aimsp.intelligence.domain.insight.InsightArticle;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InsightDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String insightType;
        private String competitor;
        private Integer impactScore;
        private List<String> actionItems;
        private int sourceArticleCount;
        private LocalDateTime generatedAt;

        public static Response from(Insight insight) {
            return Response.builder()
                    .id(insight.getId())
                    .title(insight.getTitle())
                    .content(insight.getContent())
                    .insightType(insight.getInsightType())
                    .competitor(insight.getCompetitor())
                    .impactScore(insight.getImpactScore())
                    .actionItems(new ArrayList<>(insight.getActionItems()))
                    .sourceArticleCount(insight.getSourceArticles() != null ? insight.getSourceArticles().size() : 0)
                    .generatedAt(insight.getGeneratedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class SourceArticleResponse {
        private Long id;
        private String title;
        private String url;
        private String competitor;
        private String category;
        private String summary;
        private LocalDateTime publishedAt;
        private Integer relevanceScore;

        public static SourceArticleResponse from(InsightArticle ia) {
            var a = ia.getArticle();
            return SourceArticleResponse.builder()
                    .id(a.getId())
                    .title(a.getTitle())
                    .url(a.getUrl())
                    .competitor(a.getCompetitor())
                    .category(a.getCategory())
                    .summary(a.getSummary())
                    .publishedAt(a.getPublishedAt())
                    .relevanceScore(ia.getRelevanceScore())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String insightType;
        private String competitor;
        private Integer impactScore;
        private List<String> actionItems;
        private List<SourceArticleResponse> sourceArticles;
        private LocalDateTime generatedAt;

        public static DetailResponse from(Insight insight) {
            List<SourceArticleResponse> articles = insight.getSourceArticles() != null
                    ? insight.getSourceArticles().stream().map(SourceArticleResponse::from).collect(Collectors.toList())
                    : List.of();

            return DetailResponse.builder()
                    .id(insight.getId())
                    .title(insight.getTitle())
                    .content(insight.getContent())
                    .insightType(insight.getInsightType())
                    .competitor(insight.getCompetitor())
                    .impactScore(insight.getImpactScore())
                    .actionItems(new ArrayList<>(insight.getActionItems()))
                    .sourceArticles(articles)
                    .generatedAt(insight.getGeneratedAt())
                    .build();
        }
    }
}
