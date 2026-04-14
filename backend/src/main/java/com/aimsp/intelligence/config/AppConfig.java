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

    public String getGeminiApiKey() { return geminiApiKey; }
    public String getGeminiApiUrl() { return geminiApiUrl; }
    public String getGeminiModel()  { return geminiModel; }
    public int getMaxTokens()       { return maxTokens; }
}
