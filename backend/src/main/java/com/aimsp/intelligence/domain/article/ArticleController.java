package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.crawler.CrawlerOrchestrator;
import com.aimsp.intelligence.dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final CrawlerOrchestrator crawlerOrchestrator;

    // 기사 목록 조회
    @GetMapping
    public ResponseEntity<Page<ArticleDto.Response>> getArticles(
            @RequestParam(required = false) String competitor,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                articleService.getArticles(competitor, category, sourceType, keyword, dateFrom, dateTo, page, size)
        );
    }

    // 기사 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDto.Response> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticle(id));
    }

    // 수동 크롤링 트리거
    @PostMapping("/crawl")
    public ResponseEntity<Map<String, Object>> triggerCrawl() {
        log.info("수동 크롤링 시작");
        int count = crawlerOrchestrator.crawlAll();
        Map<String, Object> result = new HashMap<>();
        result.put("crawledCount", count);
        result.put("triggeredAt", LocalDateTime.now());
        return ResponseEntity.ok(result);
    }

    // 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(articleService.getStats());
    }
}
