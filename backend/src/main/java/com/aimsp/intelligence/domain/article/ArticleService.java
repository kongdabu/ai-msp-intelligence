package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.ai.SummaryGenerator;
import com.aimsp.intelligence.dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final SummaryGenerator summaryGenerator;

    // 기사 목록 조회 (필터 적용)
    @Transactional(readOnly = true)
    public Page<ArticleDto.Response> getArticles(
            String competitor, String category, String sourceType,
            String keyword, LocalDateTime dateFrom, LocalDateTime dateTo,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findWithFilters(
                competitor, category, sourceType, keyword, dateFrom, dateTo, pageable
        ).map(ArticleDto.Response::from);
    }

    // 기사 상세 조회
    @Transactional(readOnly = true)
    public ArticleDto.Response getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다: " + id));
        return ArticleDto.Response.from(article);
    }

    // 기사 저장 (중복 URL 스킵)
    @Transactional
    public Article saveIfNotExists(Article article) {
        if (articleRepository.existsByUrl(article.getUrl())) {
            log.debug("중복 URL 스킵: {}", article.getUrl());
            return null;
        }
        article.setCollectedAt(LocalDateTime.now());
        Article saved = articleRepository.save(article);
        log.info("기사 저장: {}", saved.getTitle());
        return saved;
    }

    // 통계 조회
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 경쟁사별 기사 수
        List<Object[]> competitorStats = articleRepository.countByCompetitor();
        Map<String, Long> competitorMap = competitorStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        stats.put("byCompetitor", competitorMap);

        // 7일 카테고리 트렌드
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> categoryTrend = articleRepository.countByCategoryAndDate(since);
        stats.put("categoryTrend", categoryTrend);

        // 오늘 수집 수
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        stats.put("todayCount", articleRepository.countTodayArticles(startOfDay));
        stats.put("totalCount", articleRepository.count());

        return stats;
    }

    // 미처리 기사 목록
    @Transactional(readOnly = true)
    public List<Article> getUnprocessedArticles() {
        return articleRepository.findByIsProcessedFalseOrderByCollectedAtDesc();
    }

    // 처리 완료 표시
    @Transactional
    public void markAsProcessed(Long id) {
        articleRepository.findById(id).ifPresent(article -> {
            article.setIsProcessed(true);
            articleRepository.save(article);
        });
    }

    // 최신 기사 5건
    @Transactional(readOnly = true)
    public List<ArticleDto.Response> getLatestArticles() {
        return articleRepository.findTop5ByOrderByCollectedAtDesc()
                .stream()
                .map(ArticleDto.Response::from)
                .collect(Collectors.toList());
    }
}
