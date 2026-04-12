package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.article.Article;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ArticleDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String url;
        private String title;
        private String summary;
        private String competitor;
        private String category;
        private String sourceType;
        private String sourceName;
        private LocalDateTime publishedAt;
        private LocalDateTime collectedAt;
        private Boolean isProcessed;
        private Integer relevanceScore;

        public static Response from(Article article) {
            return Response.builder()
                    .id(article.getId())
                    .url(article.getUrl())
                    .title(article.getTitle())
                    .summary(article.getSummary())
                    .competitor(article.getCompetitor())
                    .category(article.getCategory())
                    .sourceType(article.getSourceType())
                    .sourceName(article.getSourceName())
                    .publishedAt(article.getPublishedAt())
                    .collectedAt(article.getCollectedAt())
                    .isProcessed(article.getIsProcessed())
                    .relevanceScore(article.getRelevanceScore())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String url;
        private String title;
        private String originalContent;
        private String summary;
        private String competitor;
        private String category;
        private String sourceType;
        private String sourceName;
        private LocalDateTime publishedAt;
        private LocalDateTime collectedAt;
        private Boolean isProcessed;
        private Integer relevanceScore;

        public static DetailResponse from(Article article) {
            return DetailResponse.builder()
                    .id(article.getId())
                    .url(article.getUrl())
                    .title(article.getTitle())
                    .originalContent(article.getOriginalContent())
                    .summary(article.getSummary())
                    .competitor(article.getCompetitor())
                    .category(article.getCategory())
                    .sourceType(article.getSourceType())
                    .sourceName(article.getSourceName())
                    .publishedAt(article.getPublishedAt())
                    .collectedAt(article.getCollectedAt())
                    .isProcessed(article.getIsProcessed())
                    .relevanceScore(article.getRelevanceScore())
                    .build();
        }
    }
}
