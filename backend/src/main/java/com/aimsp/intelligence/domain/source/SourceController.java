package com.aimsp.intelligence.domain.source;

import com.aimsp.intelligence.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    // 허용 스키마: http/https만 허용 (SSRF 방어)
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    // 차단할 내부망 호스트 패턴 (SSRF 방어)
    private static final List<String> BLOCKED_HOSTS = List.of(
            "localhost", "127.", "10.", "192.168.", "172.16.", "172.17.", "172.18.",
            "172.19.", "172.20.", "172.21.", "172.22.", "172.23.", "172.24.",
            "172.25.", "172.26.", "172.27.", "172.28.", "172.29.", "172.30.",
            "172.31.", "169.254.", "::1", "0.0.0.0"
    );

    private final SourceService sourceService;

    @GetMapping
    public ResponseEntity<List<SourceDto.Response>> getSources() {
        return ResponseEntity.ok(sourceService.getAllSources());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<SourceDto.Response> toggleSource(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.toggleSource(id));
    }

    @PostMapping
    public ResponseEntity<?> addSource(@RequestBody SourceDto.CreateRequest request) {
        String validationError = validateUrl(request.getUrl());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }
        return ResponseEntity.ok(sourceService.addSource(request));
    }

    private String validateUrl(String url) {
        if (url == null || url.isBlank()) return "URL이 비어 있습니다.";
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
                return "허용되지 않는 URL 스키마입니다. http 또는 https만 사용 가능합니다.";
            }
            String host = uri.getHost();
            if (host == null) return "유효하지 않은 URL입니다.";
            String lowerHost = host.toLowerCase();
            for (String blocked : BLOCKED_HOSTS) {
                if (lowerHost.equals(blocked) || lowerHost.startsWith(blocked)) {
                    return "내부 네트워크 주소는 등록할 수 없습니다.";
                }
            }
            return null;
        } catch (Exception e) {
            return "유효하지 않은 URL 형식입니다.";
        }
    }
}
