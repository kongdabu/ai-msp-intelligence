package com.aimsp.intelligence.crawler.sources;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GoogleNewsRssFetcher {

    private static final int MAX_ATTEMPTS = 3;
    private static final long[] RETRY_DELAYS_MS = {2000, 4000};

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public String fetch(String url) throws Exception {
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
                    // 빈 바디 = Google 봇 챌린지 페이지 — 재시도
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
