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
                        .header("User-Agent", "Mozilla/5.0 (compatible; RSSReader/1.0)")
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().string();
                    }
                    int code = response.code();
                    // 503/429는 일시적 오류 — 재시도
                    if (code == 503 || code == 429) {
                        lastException = new IllegalStateException("HTTP " + code);
                        continue;
                    }
                    throw new IllegalStateException("HTTP " + code);
                }
            } catch (IllegalStateException e) {
                lastException = e;
            }
        }
        throw lastException != null ? lastException
                : new IllegalStateException("RSS 수신 실패: " + url);
    }
}
