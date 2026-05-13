package com.aimsp.intelligence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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

    @Value("${app.procurement.api-key:}")
    private String procurementApiKey;

    @Value("${app.saramin.api-key:}")
    private String saraminApiKey;

    @Value("${app.crawl.request-delay-ms:2000}")
    private long requestDelayMs;

    public String getGeminiApiKey() { return geminiApiKey; }
    public String getGeminiApiUrl() { return geminiApiUrl; }
    public String getGeminiModel()  { return geminiModel; }
    public int getMaxTokens()       { return maxTokens; }
    public long getRateLimitMs()    { return rateLimitMs; }
    public String getNaverClientId()     { return naverClientId; }
    public String getNaverClientSecret() { return naverClientSecret; }
    public String getProcurementApiKey() { return procurementApiKey; }
    public String getSaraminApiKey()     { return saraminApiKey; }
    public long getRequestDelayMs()      { return requestDelayMs; }
}
