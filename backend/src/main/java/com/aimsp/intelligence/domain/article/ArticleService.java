package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.ai.EmbeddingApiClient;
import com.aimsp.intelligence.ai.SummaryGenerator;
import com.aimsp.intelligence.dto.ArticleDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final EmbeddingApiClient embeddingApiClient;

    // 기사 목록 조회 - DB LIMIT 적용 (경쟁사 분석 페이지용)
    // findAll(spec, sort) 대신 PageRequest를 사용해 DB 레벨에서 LIMIT 처리
    @Transactional(readOnly = true)
    public List<ArticleDto.Response> getArticlesList(
            String competitor, String category, LocalDateTime dateFrom, LocalDateTime dateTo, int limit) {

        Specification<Article> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (competitor != null) predicates.add(cb.equal(root.get("competitor"), competitor));
            if (category != null)   predicates.add(cb.equal(root.get("category"), category));
            if (dateFrom != null)   predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), dateFrom));
            if (dateTo != null)     predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), dateTo));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return articleRepository.findAll(spec, pageable).getContent().stream()
                .map(ArticleDto.Response::from)
                .collect(Collectors.toList());
    }

    // 기사 목록 조회 (필터 적용)
    // Specification 사용 - null 조건은 쿼리에서 제외하여 PostgreSQL 타입 추론 오류 방지
    @Transactional(readOnly = true)
    public Page<ArticleDto.Response> getArticles(
            String competitor, String category, String sourceType,
            String keyword, LocalDateTime dateFrom, LocalDateTime dateTo,
            int page, int size) {

        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.toLowerCase() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));

        Specification<Article> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (competitor != null)       predicates.add(cb.equal(root.get("competitor"), competitor));
            if (category != null)         predicates.add(cb.equal(root.get("category"), category));
            if (sourceType != null)       predicates.add(cb.equal(root.get("sourceType"), sourceType));
            if (normalizedKeyword != null) {
                String pattern = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern)
                ));
            }
            if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(root.get("publishedAt"), dateFrom));
            if (dateTo != null)   predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), dateTo));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return articleRepository.findAll(spec, pageable).map(ArticleDto.Response::from);
    }

    // 기사 상세 조회
    @Transactional(readOnly = true)
    public ArticleDto.Response getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다: " + id));
        return ArticleDto.Response.from(article);
    }

    // URL 중복 여부 확인 (Gemini 호출 전 선제 체크용)
    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return articleRepository.existsByUrl(url);
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

    // 최신 기사 5건 (발행일 기준)
    @Transactional(readOnly = true)
    public List<ArticleDto.Response> getLatestArticles() {
        return articleRepository.findTop5ByOrderByPublishedAtDesc()
                .stream()
                .map(ArticleDto.Response::from)
                .collect(Collectors.toList());
    }

    /**
     * 기존 기사 임베딩 일괄 생성 (백필).
     * summary가 있고 embedding이 없는 기사를 batchSize씩 처리.
     * @return {total, done, failed}
     */
    public Map<String, Long> backfillEmbeddings(int batchSize) {
        long total = articleRepository.countByEmbeddingIsNull();
        long done = 0;
        long failed = 0;
        int page = 0;

        log.info("[임베딩 백필] 시작: 대상 {}건, 배치 크기 {}", total, batchSize);

        while (true) {
            List<Article> batch = articleRepository.findByEmbeddingIsNull(
                    PageRequest.of(page, batchSize)
            );
            if (batch.isEmpty()) break;

            for (Article article : batch) {
                try {
                    String text = (article.getTitle() != null ? article.getTitle() : "") +
                            " " + (article.getSummary() != null ? article.getSummary() : "");
                    float[] embedding = embeddingApiClient.embed(
                            text.trim(), EmbeddingApiClient.TaskType.RETRIEVAL_DOCUMENT
                    );
                    if (embedding != null) {
                        articleRepository.updateEmbedding(
                                article.getId(),
                                EmbeddingApiClient.toVectorString(embedding)
                        );
                        done++;
                    } else {
                        failed++;
                    }
                    // API rate limit 방지 (약 100 RPM 제한)
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("[임베딩 백필] 실패 [id={}]: {}", article.getId(), e.getMessage());
                    failed++;
                }
            }

            log.info("[임베딩 백필] 진행: {}건 완료, {}건 실패", done, failed);
            // 마지막 페이지였으면 종료
            if (batch.size() < batchSize) break;
        }

        log.info("[임베딩 백필] 완료: 전체 {} / 성공 {} / 실패 {}", total, done, failed);
        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("done", done);
        result.put("failed", failed);
        return result;
    }
}
