package com.aimsp.intelligence.crawler.sources;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GoogleNewsRssFetcher {

    private static final int MAX_ATTEMPTS = 3;
    // 재시도 간격: 10s, 20s (503 rate limit 해소 대기)
    private static final long[] RETRY_DELAYS_MS = {10_000, 20_000};
    // 첫 요청 전 지터: 0~8초 랜덤 (병렬 크롤러 간 요청 분산)
    private static final int JITTER_MAX_MS = 8_000;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final Random random = new Random();

    public String fetch(String url) throws Exception {
        // 병렬 크롤러가 동시에 Google News를 호출하지 않도록 초기 지터 적용
        Thread.sleep(random.nextInt(JITTER_MAX_MS));

        Exception lastException = null;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            if (attempt > 0) {
                Thread.sleep(RETRY_DELAYS_MS[attempt - 1]);
                log.warn("Google News RSS 재시도 {}/{}: {}", attempt, MAX_ATTEMPTS - 1, url);
            }
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                        .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8")
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    int code = response.code();
                    if (!response.isSuccessful() || response.body() == null) {
                        lastException = new IllegalStateException("HTTP " + code);
                        continue;
                    }
                    String body = response.body().string();
                    if (body.isBlank()) {
                        lastException = new IllegalStateException("HTTP " + code + " (empty body)");
                        continue;
                    }
                    return body;
                }
            } catch (IllegalStateException e) {
                lastException = e;
            }
        }
        throw lastException != null ? lastException
                : new IllegalStateException("RSS 수신 실패: " + url);
    }
}
