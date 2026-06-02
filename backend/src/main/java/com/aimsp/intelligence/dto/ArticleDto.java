package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.article.Article;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        private boolean bookmarked;
        private LocalDateTime bookmarkedAt;
        private String bookmarkNote;

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
                    .bookmarked(Boolean.TRUE.equals(article.getBookmarked()))
                    .bookmarkedAt(article.getBookmarkedAt())
                    .bookmarkNote(article.getBookmarkNote())
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
        private boolean bookmarked;
        private LocalDateTime bookmarkedAt;
        private String bookmarkNote;

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
                    .bookmarked(Boolean.TRUE.equals(article.getBookmarked()))
                    .bookmarkedAt(article.getBookmarkedAt())
                    .bookmarkNote(article.getBookmarkNote())
                    .build();
        }
    }

    // 북마크 토글/메모 갱신 요청
    @Getter
    @Setter
    @NoArgsConstructor
    public static class BookmarkRequest {
        private Boolean bookmarked; // true=저장, false=해제
        private String note;        // 리마인드 메모 (선택)
    }
}
