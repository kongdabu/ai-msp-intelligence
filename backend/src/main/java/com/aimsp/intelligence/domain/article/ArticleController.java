package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.crawler.CrawlJobService;
import com.aimsp.intelligence.config.TaskExecutionLogger;
import com.aimsp.intelligence.dto.ArticleDto;
import com.aimsp.intelligence.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final CrawlJobService crawlJobService;

    // 기사 목록 조회 - List (COUNT 쿼리 없음, 경쟁사 분석 페이지용)
    @GetMapping("/list")
    public ResponseEntity<List<ArticleDto.Response>> getArticlesList(
            @RequestParam(required = false) String competitor,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "50") int limit) {

        return ResponseEntity.ok(
                articleService.getArticlesList(competitor, category, dateFrom, dateTo, Math.min(Math.max(limit, 1), 100))
        );
    }

    // 기사 목록 조회
    @GetMapping
    public ResponseEntity<PageResponseDto<ArticleDto.Response>> getArticles(
            @RequestParam(required = false) String competitor,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(
                articleService.getArticles(competitor, category, sourceType, keyword, dateFrom, dateTo, safePage, safeSize)
        );
    }

    // 저장(북마크)된 기사 목록 조회
    @GetMapping("/bookmarked")
    public ResponseEntity<PageResponseDto<ArticleDto.Response>> getBookmarkedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        return ResponseEntity.ok(articleService.getBookmarkedArticles(safePage, safeSize));
    }

    // 기사 상세 조회
    @GetMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<ArticleDto.Response> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticle(id));
    }

    // 기사 저장(북마크) 토글 / 메모 갱신
    @PutMapping("/{id}/bookmark")
    @SuppressWarnings("null")
    public ResponseEntity<ArticleDto.Response> toggleBookmark(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody ArticleDto.BookmarkRequest request) {
        return ResponseEntity.ok(articleService.toggleBookmark(id, request.getBookmarked(), request.getNote()));
    }

    // 수동 크롤링 트리거 (AI 생태계·사업모델 뉴스 + 활성 RSS)
    @PostMapping("/crawl")
    public ResponseEntity<CrawlJobService.JobStatus> triggerCrawl() {
        TaskExecutionLogger.logStart(log, "API 실행: 기사 수집 요청");
        CrawlJobService.JobStatus status = crawlJobService.start();
        return ResponseEntity.accepted().body(status);
    }

    @GetMapping("/crawl/status")
    public ResponseEntity<CrawlJobService.JobStatus> getCrawlStatus() {
        return ResponseEntity.ok(crawlJobService.getStatus());
    }

    @DeleteMapping("/crawl")
    public ResponseEntity<CrawlJobService.JobStatus> cancelCrawl() {
        TaskExecutionLogger.logStart(log, "API 실행: 기사 수집 취소 요청");
        return ResponseEntity.ok(crawlJobService.cancel());
    }

    // 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(articleService.getStats());
    }
}
