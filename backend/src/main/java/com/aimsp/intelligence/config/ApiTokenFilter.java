package com.aimsp.intelligence.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class ApiTokenFilter extends OncePerRequestFilter {

    @Value("${app.api.secret-token:}")
    private String secretToken;

    // 토큰 검증이 필요한 변조성 엔드포인트
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/api/articles/crawl",
            "/api/insights/generate",
            "/api/battlecards/generate",
            "/api/sources",
            "/api/admin/config"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String method = request.getMethod();
        String path   = request.getRequestURI();

        boolean needsToken = secretToken != null && !secretToken.isBlank()
                && isProtected(method, path);

        if (needsToken) {
            String provided = request.getHeader("X-API-Token");
            if (!secretToken.equals(provided)) {
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
        // /api/sources POST, /api/admin/config PUT 등 정확 매칭 또는 prefix
        return PROTECTED_PATHS.stream().anyMatch(path::startsWith);
    }
}
