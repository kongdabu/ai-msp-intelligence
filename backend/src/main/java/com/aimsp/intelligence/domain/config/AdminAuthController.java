package com.aimsp.intelligence.domain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Value("${app.api.secret-token:}")
    private String secretToken;

    @GetMapping("/auth-status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        boolean tokenRequired = secretToken != null && !secretToken.isBlank();
        return ResponseEntity.ok(Map.of(
                "tokenRequired", tokenRequired
        ));
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken() {
        // ApiTokenFilter를 정상 통과했으므로 유효한 토큰임
        return ResponseEntity.ok(Map.of(
                "valid", true
        ));
    }
}
