package com.aimsp.intelligence.domain.radar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class RadarSourceVerifier {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(15))
            .followRedirects(true)
            .build();

    // 404·410만 삭제·만료로 확정한다. 접근 차단과 네트워크 오류는 정상 원문을 잘못 제외하지 않도록 보류한다.
    public SourceCheckResult check(String sourceUrl) {
        try {
            Request request = new Request.Builder()
                    .url(sourceUrl)
                    .header("User-Agent", "AI-MSP-Intelligence/1.0")
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404 || response.code() == 410) return SourceCheckResult.unavailable();
                if (response.isSuccessful() || response.isRedirect()) return SourceCheckResult.available();
                return SourceCheckResult.unknown();
            }
        } catch (IllegalArgumentException | IOException e) {
            return SourceCheckResult.unknown();
        }
    }

    public record SourceCheckResult(CheckStatus status) {
        private static SourceCheckResult available() {
            return new SourceCheckResult(CheckStatus.AVAILABLE);
        }

        private static SourceCheckResult unavailable() {
            return new SourceCheckResult(CheckStatus.UNAVAILABLE);
        }

        private static SourceCheckResult unknown() {
            return new SourceCheckResult(CheckStatus.UNKNOWN);
        }
    }

    public enum CheckStatus {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN
    }
}
