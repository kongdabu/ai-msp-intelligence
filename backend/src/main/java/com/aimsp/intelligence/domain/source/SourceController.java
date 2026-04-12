package com.aimsp.intelligence.domain.source;

import com.aimsp.intelligence.dto.SourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    // 소스 목록 조회
    @GetMapping
    public ResponseEntity<List<SourceDto.Response>> getSources() {
        return ResponseEntity.ok(sourceService.getAllSources());
    }

    // 소스 활성/비활성 토글
    @PutMapping("/{id}/toggle")
    public ResponseEntity<SourceDto.Response> toggleSource(@PathVariable Long id) {
        return ResponseEntity.ok(sourceService.toggleSource(id));
    }

    // 소스 추가
    @PostMapping
    public ResponseEntity<SourceDto.Response> addSource(@RequestBody SourceDto.CreateRequest request) {
        return ResponseEntity.ok(sourceService.addSource(request));
    }
}
