package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeApiClient {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODELS_URL = "https://api.anthropic.com/v1/models";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final int MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 30000; // 30초
    // Claude Haiku 유료 플랜: 1000 RPM — 1초 간격으로 완화
    private static final long MIN_INTERVAL_MS = 1000;
    private final AtomicLong lastCallTime = new AtomicLong(0);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Claude API 헬스체크 - GET /v1/models (최소 비용)
     * 200 OK → true, 그 외 → false
     */
    public boolean isAvailable() {
        Request request = new Request.Builder()
                .url(MODELS_URL)
                .get()
                .header("x-api-key", appConfig.getClaudeApiKey())
                .header("anthropic-version", ANTHROPIC_VERSION)
                .build();
        try (Response response = client.newCall(request).execute()) {
            boolean ok = response.isSuccessful();
            log.info("Claude API 헬스체크: {} (HTTP {})", ok ? "정상" : "비정상", response.code());
            return ok;
        } catch (Exception e) {
            log.error("Claude API 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Claude API 호출 - 단일 텍스트 프롬프트
     * 429/529(과부하) 시 대기 후 최대 MAX_RETRIES 재시도
     */
    public String call(String prompt) {
        try {
            applyRateLimit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        String requestBody = buildRequestBody(prompt);
        if (requestBody == null) return null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(RequestBody.create(requestBody, JSON))
                        .header("x-api-key", appConfig.getClaudeApiKey())
                        .header("anthropic-version", ANTHROPIC_VERSION)
                        .header("content-type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 429 || response.code() == 529) {
                        // 429: Rate Limit, 529: API 과부하
                        long retryDelayMs = parseRetryDelay(response);
                        log.warn("Claude API 제한({}) - {}ms 후 재시도 ({}/{})",
                                response.code(), retryDelayMs, attempt, MAX_RETRIES);
                        if (attempt < MAX_RETRIES) {
                            Thread.sleep(retryDelayMs);
                            continue;
                        }
                        throw new AiApiUnavailableException();
                    }
                    if (response.code() == 503) {
                        log.warn("Claude API 일시적 과부하(503) - 10초 후 재시도 ({}/{})", attempt, MAX_RETRIES);
                        if (attempt < MAX_RETRIES) {
                            Thread.sleep(10000);
                            continue;
                        }
                        throw new AiApiUnavailableException();
                    }
                    if (response.code() >= 500) {
                        log.error("Claude API 서버 오류 (HTTP {}): 작업 중단", response.code());
                        throw new AiApiUnavailableException();
                    }
                    if (!response.isSuccessful()) {
                        String errBody = response.body() != null ? response.body().string() : "(no body)";
                        log.error("Claude API 오류: {} {} | body: {}", response.code(), response.message(), errBody);
                        return null;
                    }

                    String responseBody = response.body() != null ? response.body().string() : null;
                    if (responseBody == null) return null;

                    // 응답 파싱: content[0].text
                    JsonNode root = objectMapper.readTree(responseBody);
                    String text = root.path("content").get(0).path("text").asText();
                    return stripMarkdownCodeBlock(text);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (IOException e) {
                log.error("Claude API 호출 실패: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * 전역 Rate Limiter: SummaryGenerator/InsightGenerator 등 모든 호출자 공유
     */
    private synchronized void applyRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastCallTime.get();
        if (lastCallTime.get() > 0 && elapsed < MIN_INTERVAL_MS) {
            long waitMs = MIN_INTERVAL_MS - elapsed;
            log.debug("Rate limit 대기: {}ms", waitMs);
            Thread.sleep(waitMs);
        }
        lastCallTime.set(System.currentTimeMillis());
    }

    private String buildRequestBody(String prompt) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", appConfig.getClaudeModel());
            root.put("max_tokens", appConfig.getMaxTokens());
            root.put("system", "You must respond with valid JSON only. Do not include any text outside the JSON.");
            var messages = objectMapper.createArrayNode();
            var userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            root.set("messages", messages);
            return objectMapper.writeValueAsString(root);
        } catch (IOException e) {
            log.error("Claude 요청 바디 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Claude가 간헐적으로 붙이는 마크다운 코드블록 제거
     * ```json ... ``` 또는 ``` ... ``` 형태를 순수 JSON으로 변환
     */
    private String stripMarkdownCodeBlock(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
            }
        }
        return trimmed;
    }

    /**
     * retry-after 헤더 파싱 (없으면 기본값)
     */
    private long parseRetryDelay(Response response) {
        String retryAfter = response.header("retry-after");
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter.trim()) * 1000L;
            } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_RETRY_DELAY_MS;
    }
}
