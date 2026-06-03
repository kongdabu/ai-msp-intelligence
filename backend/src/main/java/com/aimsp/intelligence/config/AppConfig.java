package com.aimsp.intelligence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.lang.NonNull;

@Configuration
public class AppConfig {

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.gemini.api-url}")
    private String geminiApiUrl;

    @Value("${app.gemini.model}")
    private String geminiModel;

    @Value("${app.gemini.max-tokens}")
    private int maxTokens;

    @Value("${app.gemini.rate-limit-ms:6000}")
    private long rateLimitMs;

    @Value("${app.naver.client-id:}")
    private String naverClientId;

    @Value("${app.naver.client-secret:}")
    private String naverClientSecret;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:80,http://frontend:80}")
    private String corsAllowedOrigins;

    public String getGeminiApiKey()      { return geminiApiKey; }
    public String getGeminiApiUrl()      { return geminiApiUrl; }
    public String getGeminiModel()       { return geminiModel; }
    public int    getMaxTokens()         { return maxTokens; }
    public long   getRateLimitMs()       { return rateLimitMs; }
    public String getNaverClientId()     { return naverClientId; }
    public String getNaverClientSecret() { return naverClientSecret; }
    @SuppressWarnings("null")
    public @NonNull String[] getCorsAllowedOrigins() {
        if (corsAllowedOrigins == null || corsAllowedOrigins.isBlank()) {
            return new String[0];
        }
        return java.util.Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }
}
