package com.aimsp.intelligence.domain.radar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;

@Component
public class RadarSourceVerifier {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(15))
            // 리다이렉트 목적지는 별도 검증이 되지 않으므로 서버 요청을 따라가지 않는다.
            .followRedirects(false)
            .build();

    // 404·410만 삭제·만료로 확정한다. 접근 차단과 네트워크 오류는 정상 원문을 잘못 제외하지 않도록 보류한다.
    public SourceCheckResult check(String sourceUrl) {
        try {
            if (!isPublicHttpUrl(sourceUrl)) return SourceCheckResult.rejected();
            Request request = new Request.Builder()
                    .url(sourceUrl)
                    .header("User-Agent", "AI-MSP-Intelligence/1.0")
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404 || response.code() == 410) return SourceCheckResult.unavailable();
                if (response.isSuccessful()) return SourceCheckResult.available();
                return SourceCheckResult.unknown();
            }
        } catch (IllegalArgumentException | IOException e) {
            return SourceCheckResult.unknown();
        }
    }

    private boolean isPublicHttpUrl(String sourceUrl) {
        try {
            URI uri = URI.create(sourceUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) return false;
            if (uri.getHost() == null || uri.getUserInfo() != null) return false;
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress() || address.isMulticastAddress()) return false;
            }
            return true;
        } catch (IllegalArgumentException | UnknownHostException e) {
            return false;
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

        private static SourceCheckResult rejected() {
            return new SourceCheckResult(CheckStatus.REJECTED);
        }
    }

    public enum CheckStatus {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN,
        REJECTED
    }
}
