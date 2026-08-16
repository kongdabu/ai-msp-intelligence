package com.aimsp.intelligence.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 비용이 발생하는 수동 실행 API의 단순 고정 구간 요청 제한이다. */
@Slf4j
@Component
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, Window> WINDOWS = new ConcurrentHashMap<>();

    @Value("${app.api.rate-limit.manual-jobs-per-minute:5}")
    private int manualJobsPerMinute;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (!isManualJobRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        String key = clientIp + ':' + request.getRequestURI();
        if (!tryAcquire(key)) {
            log.warn("수동 작업 요청 제한 초과: path={} ip={}", request.getRequestURI(), clientIp);
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean tryAcquire(String key) {
        Instant now = Instant.now();
        if (WINDOWS.size() > 10_000) {
            WINDOWS.entrySet().removeIf(entry -> Duration.between(entry.getValue().startedAt(), now).toSeconds() >= 60);
        }
        Window window = WINDOWS.compute(key, (ignored, current) -> {
            if (current == null || Duration.between(current.startedAt(), now).toSeconds() >= 60) {
                return new Window(now, new AtomicInteger(1));
            }
            current.count().incrementAndGet();
            return current;
        });
        return window.count().get() <= Math.max(1, manualJobsPerMinute);
    }

    private boolean isManualJobRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return false;
        return switch (request.getRequestURI()) {
            case "/api/articles/crawl", "/api/insights/generate", "/api/trends/generate",
                    "/api/battlecards/generate", "/api/radar/weekly-briefs/generate", "/api/radar/collect" -> true;
            default -> false;
        };
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
        return request.getRemoteAddr();
    }

    private record Window(Instant startedAt, AtomicInteger count) {
    }
}
