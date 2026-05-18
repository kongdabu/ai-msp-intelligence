package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.config.SystemConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SystemConfigDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private int maxArticlesForInsight;
        private int maxInsightsPerGeneration;

        public static Response from(SystemConfig config) {
            Response dto = new Response();
            dto.maxArticlesForInsight    = config.getMaxArticlesForInsight();
            dto.maxInsightsPerGeneration = config.getMaxInsightsPerGeneration();
            return dto;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Min(1) @Max(500)
        private int maxArticlesForInsight;

        @Min(1) @Max(50)
        private int maxInsightsPerGeneration;
    }
}
