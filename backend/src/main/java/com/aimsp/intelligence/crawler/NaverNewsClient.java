package com.aimsp.intelligence.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aimsp.intelligence.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsClient {

    private static final String NEWS_URL = "https://openapi.naver.com/v1/search/news.json";

    private final AppConfig appConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<NaverNewsItem> search(String query) throws Exception {
        if (appConfig.getNaverClientId().isBlank() || appConfig.getNaverClientSecret().isBlank()) {
            log.warn("Naver API 키 미설정 — 뉴스 수집 스킵: {}", query);
            return List.of();
        }

        HttpUrl url = HttpUrl.parse(NEWS_URL).newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("display", "20")
                .addQueryParameter("sort", "date")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("X-Naver-Client-Id", appConfig.getNaverClientId())
                .header("X-Naver-Client-Secret", appConfig.getNaverClientSecret())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IllegalStateException("Naver API HTTP " + response.code());
            }
            NaverSearchResponse result = objectMapper.readValue(response.body().string(), NaverSearchResponse.class);
            return result.items() != null ? result.items() : List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverSearchResponse(List<NaverNewsItem> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaverNewsItem(String title, String originallink, String link, String description, String pubDate) {

        private static final DateTimeFormatter PUB_DATE_FMT =
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        public String cleanTitle() {
            return title != null ? title.replaceAll("<[^>]+>", "")
                    .replace("&quot;", "\"").replace("&amp;", "&").replace("&#039;", "'") : "";
        }

        public String cleanDescription() {
            return description != null ? description.replaceAll("<[^>]+>", "")
                    .replace("&quot;", "\"").replace("&amp;", "&").replace("&#039;", "'") : "";
        }

        public LocalDateTime parsedDate() {
            if (pubDate == null || pubDate.isBlank()) return LocalDateTime.now();
            try {
                return java.time.ZonedDateTime.parse(pubDate.trim(), PUB_DATE_FMT)
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime();
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }

        // 네이버 뉴스 링크보다 원본 기사 URL 우선 사용
        public String bestUrl() {
            return (originallink != null && !originallink.isBlank()) ? originallink : link;
        }
    }
}
