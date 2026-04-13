package com.aimsp.intelligence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.claude.api-key}")
    private String claudeApiKey;

    @Value("${app.claude.model}")
    private String claudeModel;

    @Value("${app.claude.max-tokens}")
    private int maxTokens;

    public String getClaudeApiKey() {
        return claudeApiKey;
    }

    public String getClaudeModel() {
        return claudeModel;
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}
