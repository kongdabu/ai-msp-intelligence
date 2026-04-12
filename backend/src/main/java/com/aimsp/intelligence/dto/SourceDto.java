package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.source.Source;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class SourceDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String url;
        private String type;
        private String competitor;
        private Boolean active;
        private LocalDateTime lastCrawledAt;
        private Integer crawlCount;
        private Integer errorCount;

        public static Response from(Source source) {
            return Response.builder()
                    .id(source.getId())
                    .name(source.getName())
                    .url(source.getUrl())
                    .type(source.getType())
                    .competitor(source.getCompetitor())
                    .active(source.getActive())
                    .lastCrawledAt(source.getLastCrawledAt())
                    .crawlCount(source.getCrawlCount())
                    .errorCount(source.getErrorCount())
                    .build();
        }
    }

    @Getter
    public static class CreateRequest {
        private String name;
        private String url;
        private String type;
        private String competitor;
    }
}
