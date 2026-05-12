package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.battlecard.BattleCard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class BattleCardDto {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String competitor;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> opportunities;
        private List<String> threats;
        private String ourStrategy;
        private Integer impactScore;
        private int sourceArticleCount;
        private LocalDateTime generatedAt;

        public static Response from(BattleCard bc) {
            return Response.builder()
                    .id(bc.getId())
                    .competitor(bc.getCompetitor())
                    .strengths(parseList(bc.getStrengths()))
                    .weaknesses(parseList(bc.getWeaknesses()))
                    .opportunities(parseList(bc.getOpportunities()))
                    .threats(parseList(bc.getThreats()))
                    .ourStrategy(bc.getOurStrategy())
                    .impactScore(bc.getImpactScore())
                    .sourceArticleCount(bc.getSourceArticles() != null ? bc.getSourceArticles().size() : 0)
                    .generatedAt(bc.getGeneratedAt())
                    .build();
        }

        private static List<String> parseList(String json) {
            if (json == null || json.isBlank()) return List.of();
            try {
                return MAPPER.readValue(json, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                return List.of();
            }
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long id;
        private String competitor;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> opportunities;
        private List<String> threats;
        private String ourStrategy;
        private Integer impactScore;
        private LocalDateTime generatedAt;
        private List<SourceArticleItem> sourceArticles;

        @Getter
        @Builder
        public static class SourceArticleItem {
            private Long id;
            private String title;
            private String url;
            private String competitor;
            private Integer relevanceScore;
        }

        public static DetailResponse from(BattleCard bc) {
            List<SourceArticleItem> articles = bc.getSourceArticles() == null ? List.of() :
                    bc.getSourceArticles().stream()
                            .filter(bca -> bca.getArticle() != null)
                            .map(bca -> SourceArticleItem.builder()
                                    .id(bca.getArticle().getId())
                                    .title(bca.getArticle().getTitle())
                                    .url(bca.getArticle().getUrl())
                                    .competitor(bca.getArticle().getCompetitor())
                                    .relevanceScore(bca.getRelevanceScore())
                                    .build())
                            .toList();

            return DetailResponse.builder()
                    .id(bc.getId())
                    .competitor(bc.getCompetitor())
                    .strengths(Response.parseList(bc.getStrengths()))
                    .weaknesses(Response.parseList(bc.getWeaknesses()))
                    .opportunities(Response.parseList(bc.getOpportunities()))
                    .threats(Response.parseList(bc.getThreats()))
                    .ourStrategy(bc.getOurStrategy())
                    .impactScore(bc.getImpactScore())
                    .generatedAt(bc.getGeneratedAt())
                    .sourceArticles(articles)
                    .build();
        }
    }
}
