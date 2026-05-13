package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.config.AppConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobPostingApiClient {

    private static final String API_URL = "https://oapi.saramin.co.kr/job-search";

    private final AppConfig appConfig;
    private final AtomicLong lastCallTime = new AtomicLong(0);

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private synchronized void applyRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastCallTime.get();
        long minInterval = appConfig.getRequestDelayMs();
        if (lastCallTime.get() > 0 && elapsed < minInterval) {
            Thread.sleep(minInterval - elapsed);
        }
        lastCallTime.set(System.currentTimeMillis());
    }

    public List<JobItem> searchByCompany(String companyName, int count) {
        try {
            applyRateLimit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
        if (appConfig.getSaraminApiKey().isBlank()) {
            log.warn("[사람인] API 키 미설정 — 수집 스킵: {}", companyName);
            return List.of();
        }

        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("access-key", appConfig.getSaraminApiKey())
                .addQueryParameter("keywords", companyName)
                .addQueryParameter("count", String.valueOf(count))
                .addQueryParameter("fields", "category,position,company,salary,close-type")
                .addQueryParameter("job_type", "1") // 정규직 위주
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("[사람인] API HTTP {}: {}", response.code(), companyName);
                return List.of();
            }
            SaraminResponse result = objectMapper.readValue(response.body().string(), SaraminResponse.class);
            if (result.jobs() == null || result.jobs().job() == null) return List.of();
            return result.jobs().job();
        } catch (Exception e) {
            log.error("[사람인] API 호출 실패 [{}]: {}", companyName, e.getMessage());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SaraminResponse(Jobs jobs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Jobs(int count, int total, List<JobItem> job) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobItem(
            String id,
            JobUrl url,
            Position position,
            Company company,
            Long openingTimestamp
    ) {
        public LocalDateTime postingDate() {
            if (openingTimestamp == null) return LocalDateTime.now();
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(openingTimestamp), ZoneId.of("Asia/Seoul"));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JobUrl(String detail) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Position(String title, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Company(CompanyDetail detail) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompanyDetail(String name) {}
}
