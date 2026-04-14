package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.insight.Insight;
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
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String insightType;
        private String competitor;
        private Integer impactScore;
        private List<String> actionItems;
        private List<ArticleDto.Response> sourceArticles;
        private LocalDateTime generatedAt;

        public static DetailResponse from(Insight insight) {
            List<ArticleDto.Response> articles = insight.getSourceArticles() != null
                    ? insight.getSourceArticles().stream().map(ArticleDto.Response::from).collect(Collectors.toList())
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
