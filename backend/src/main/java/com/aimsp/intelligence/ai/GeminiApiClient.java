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
    private final AtomicLong lastCallTime = new AtomicLong(0);
    private final AtomicLong cooldownUntil = new AtomicLong(0);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Gemini API 헬스체크 - GET /models/{model} (토큰 소비 없음)
     */
    public boolean isAvailable() {
        String healthUrl = appConfig.getGeminiApiUrl() + "/" + appConfig.getGeminiModel();
        Request request = new Request.Builder()
                .url(healthUrl)
                .header("x-goog-api-key", appConfig.getGeminiApiKey())
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            boolean ok = response.isSuccessful();
            log.info("Gemini API 헬스체크: {} (HTTP {})", ok ? "정상" : "비정상", response.code());
            return ok;
        } catch (Exception e) {
            log.error("Gemini API 헬스체크 실패: {}", maskKey(e.getMessage()));
            return false;
        }
    }

    /** 429·503 응답이 안내한 공용 대기 시간이 남아 있는지 확인한다. */
    public boolean isCoolingDown() {
        return System.currentTimeMillis() < cooldownUntil.get();
    }

    /**
     * Gemini API 호출 - 단일 텍스트 프롬프트
     * responseMimeType: application/json 으로 JSON 응답 강제
     */
    public String call(String prompt) {
        String requestBody = buildRequestBody(prompt);
        if (requestBody == null) return null;

        String apiUrl = appConfig.getGeminiApiUrl() + "/" + appConfig.getGeminiModel()
                + ":generateContent";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                waitForPermit();
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(RequestBody.create(requestBody, JSON))
                        .header("content-type", "application/json")
                        .header("x-goog-api-key", appConfig.getGeminiApiKey())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.code() == 429) {
                        long retryDelayMs = parseRetryDelay(response);
                        log.warn("Gemini API Rate Limit(429) - {}ms 후 재시도 ({}/{})", retryDelayMs, attempt, MAX_RETRIES);
                        extendCooldown(retryDelayMs);
                        if (attempt < MAX_RETRIES) {
                            continue;
                        }
                        throw new AiApiUnavailableException();
                    }
                    if (response.code() == 503) {
                        long waitMs = 30000L * attempt; // 지수 백오프: 1차 30초, 2차 60초, 3차 90초
                        log.warn("Gemini API 일시적 과부하(503) - {}초 후 재시도 ({}/{})", waitMs / 1000, attempt, MAX_RETRIES);
                        extendCooldown(waitMs);
                        if (attempt < MAX_RETRIES) continue;
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
                    JsonNode candidates = jsonNode.path("candidates");
                    if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
                        log.error("Gemini API가 빈 후보군(candidates)을 반환했습니다. (검열 또는 모델 오류 의심)");
                        return null;
                    }
                    JsonNode candidate = candidates.get(0);
                    if (candidate == null) return null;

                    JsonNode parts = candidate.path("content").path("parts");
                    if (parts.isMissingNode() || !parts.isArray() || parts.isEmpty()) {
                        return null;
                    }
                    JsonNode part = parts.get(0);
                    if (part == null) return null;

                    return part.path("text").asText();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Gemini API 호출 중단 - 재시도하지 않음 (스레드 인터럽트)");
                return null;
            } catch (IOException e) {
                if (Thread.currentThread().isInterrupted()
                        || (e.getMessage() != null && e.getMessage().toLowerCase().contains("interrupted"))) {
                    Thread.currentThread().interrupt();
                    log.warn("Gemini API 호출 중단 - 요청 스레드가 인터럽트됨: {}", maskKey(e.getMessage()));
                    return null;
                }
                log.error("Gemini API 호출 실패: {}", maskKey(e.getMessage()));
                return null;
            }
        }
        return null;
    }

    private synchronized void waitForPermit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long rateLimitAt = lastCallTime.get() + appConfig.getRateLimitMs();
        long permittedAt = Math.max(rateLimitAt, cooldownUntil.get());
        if (now < permittedAt) {
            long waitMs = permittedAt - now;
            log.info("Gemini API 공용 대기 적용: {}ms", waitMs);
            Thread.sleep(waitMs);
        }
        lastCallTime.set(System.currentTimeMillis());
    }

    private void extendCooldown(long delayMs) {
        long nextAllowedAt = System.currentTimeMillis() + Math.max(delayMs, appConfig.getRateLimitMs());
        cooldownUntil.accumulateAndGet(nextAllowedAt, Math::max);
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

    private static String maskKey(String msg) {
        if (msg == null) return "(null)";
        return msg.replaceAll("key=[^&\\s\"]+", "key=***");
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
