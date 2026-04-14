package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class GeminiApiClient {

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 30000; // 30초
    // 15 RPM 제한: 최소 호출 간격 4초 (SummaryGenerator/InsightGenerator 공유)
    private static final long MIN_INTERVAL_MS = 4000;
    private final AtomicLong lastCallTime = new AtomicLong(0);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Gemini API 헬스체크 - GET /models/{model} (토큰 소비 없음)
     */
    public boolean isAvailable() {
        String healthUrl = appConfig.getGeminiApiUrl() + "/" + appConfig.getGeminiModel()
                + "?key=" + appConfig.getGeminiApiKey();
        Request request = new Request.Builder().url(healthUrl).get().build();
        try (Response response = client.newCall(request).execute()) {
            boolean ok = response.isSuccessful();
            log.info("Gemini API 헬스체크: {} (HTTP {})", ok ? "정상" : "비정상", response.code());
            return ok;
        } catch (Exception e) {
            log.error("Gemini API 헬스체크 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gemini API 호출 - 단일 텍스트 프롬프트
     * responseMimeType: application/json 으로 JSON 응답 강제
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

        String apiUrl = appConfig.getGeminiApiUrl() + "/" + appConfig.getGeminiModel()
                + ":generateContent?key=" + appConfig.getGeminiApiKey();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(RequestBody.create(requestBody, JSON))
                        .header("content-type", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 429) {
                        long retryDelayMs = parseRetryDelay(response);
                        log.warn("Gemini API Rate Limit(429) - {}ms 후 재시도 ({}/{})", retryDelayMs, attempt, MAX_RETRIES);
                        if (attempt < MAX_RETRIES) { Thread.sleep(retryDelayMs); continue; }
                        throw new AiApiUnavailableException();
                    }
                    if (response.code() == 503) {
                        log.warn("Gemini API 일시적 과부하(503) - 10초 후 재시도 ({}/{})", attempt, MAX_RETRIES);
                        if (attempt < MAX_RETRIES) { Thread.sleep(10000); continue; }
                        throw new AiApiUnavailableException();
                    }
                    if (response.code() >= 500) {
                        log.error("Gemini API 서버 오류 (HTTP {}): 작업 중단", response.code());
                        throw new AiApiUnavailableException();
                    }
                    if (!response.isSuccessful()) {
                        String errBody = response.body() != null ? response.body().string() : "(no body)";
                        log.error("Gemini API 오류: {} {} | body: {}", response.code(), response.message(), errBody);
                        return null;
                    }
                    String responseBody = response.body() != null ? response.body().string() : null;
                    if (responseBody == null) return null;

                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    return jsonNode.path("candidates").get(0)
                            .path("content").path("parts").get(0)
                            .path("text").asText();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (IOException e) {
                log.error("Gemini API 호출 실패: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

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
            String requestJson = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                    .set("contents", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                            .set("parts", objectMapper.createArrayNode()
                                .add(objectMapper.createObjectNode()
                                    .put("text", prompt))))));
            ObjectNode root = (ObjectNode) objectMapper.readTree(requestJson);
            ObjectNode generationConfig = objectMapper.createObjectNode();
            generationConfig.put("maxOutputTokens", appConfig.getMaxTokens());
            generationConfig.put("responseMimeType", "application/json");
            root.set("generationConfig", generationConfig);
            return objectMapper.writeValueAsString(root);
        } catch (IOException e) {
            log.error("Gemini 요청 바디 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    private long parseRetryDelay(Response response) {
        try {
            String body = response.body() != null ? response.body().string() : null;
            if (body == null) return DEFAULT_RETRY_DELAY_MS;
            JsonNode root = objectMapper.readTree(body);
            for (JsonNode detail : root.path("error").path("details")) {
                JsonNode retryDelay = detail.path("retryDelay");
                if (!retryDelay.isMissingNode()) {
                    String delayStr = retryDelay.asText().replace("s", "").trim();
                    return Long.parseLong(delayStr) * 1000L;
                }
            }
        } catch (Exception ignored) {}
        return DEFAULT_RETRY_DELAY_MS;
    }
}
