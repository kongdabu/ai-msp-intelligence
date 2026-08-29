package com.aimsp.intelligence.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Set;

@Slf4j
@Component
public class ApiTokenFilter extends OncePerRequestFilter {

    @Value("${app.api.secret-token:}")
    private String secretToken;

    // 토큰 검증이 필요한 변조성 엔드포인트
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/articles/crawl",
            "/api/articles/crawl/cancel",
            "/api/insights/generate",
            "/api/trends/generate",
            "/api/battlecards/generate",
            "/api/strategy-reports/generate",
            "/api/admin/config",
            "/api/admin/verify-token",
            "/api/radar/signals",
            "/api/radar/players",
            "/api/radar/weekly-briefs/generate",
            "/api/radar/collect",
            "/api/radar/collect/cancel",
            "/api/v1/radar/reports",
            "/api/articles/",
            "/api/insights/"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String method = request.getMethod();
        String path   = request.getRequestURI();

        boolean needsToken = secretToken != null && !secretToken.isBlank()
                && isProtected(method, path);

        if (needsToken) {
            String provided = request.getHeader("X-API-Token");
            if (provided == null || !MessageDigest.isEqual(
                    secretToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    provided.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                log.warn("API Token 인증 실패: method={} path={} ip={}",
                        method, path, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"인증이 필요합니다.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isProtected(String method, String path) {
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }
        // 변경 API만 보호한다. /api/articles/와 /api/insights/는 북마크 변경 경로다.
        return PROTECTED_PATHS.contains(path)
                || (path.startsWith("/api/articles/") && path.endsWith("/bookmark"))
                || (path.startsWith("/api/insights/") && path.endsWith("/bookmark"))
                || path.startsWith("/api/radar/players/");
    }
}
