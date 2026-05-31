package com.aimsp.intelligence.domain.insight;

import com.aimsp.intelligence.dto.InsightDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    // 인사이트 목록 조회
    @GetMapping
    public ResponseEntity<Page<InsightDto.Response>> getInsights(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String competitor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(insightService.getInsights(type, competitor, safePage, safeSize));
    }

    // 저장(북마크)된 인사이트 목록 조회
    @GetMapping("/bookmarked")
    public ResponseEntity<Page<InsightDto.Response>> getBookmarkedInsights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(insightService.getBookmarkedInsights(safePage, safeSize));
    }

    // 인사이트 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<InsightDto.DetailResponse> getInsight(@PathVariable Long id) {
        return ResponseEntity.ok(insightService.getInsight(id));
    }

    // 인사이트 저장(북마크) 토글 / 메모 갱신
    @PutMapping("/{id}/bookmark")
    public ResponseEntity<InsightDto.Response> toggleBookmark(
            @PathVariable Long id,
            @RequestBody InsightDto.BookmarkRequest request) {
        return ResponseEntity.ok(insightService.toggleBookmark(id, request.getBookmarked(), request.getNote()));
    }

    // 수동 인사이트 생성
    @PostMapping("/generate")
    public ResponseEntity<List<InsightDto.Response>> generateInsights() {
        return ResponseEntity.ok(insightService.generateInsights());
    }

    // 오늘 인사이트
    @GetMapping("/today")
    public ResponseEntity<List<InsightDto.Response>> getTodayInsights() {
        return ResponseEntity.ok(insightService.getTodayInsights());
    }
}
