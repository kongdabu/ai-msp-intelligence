package com.aimsp.intelligence.domain.insight;

import com.aimsp.intelligence.ai.InsightGenerator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import com.aimsp.intelligence.dto.InsightDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final ArticleService articleService;
    private final InsightGenerator insightGenerator;

    // 인사이트 목록 조회
    @Transactional(readOnly = true)
    public Page<InsightDto.Response> getInsights(String insightType, String competitor, int page, int size) {
        return insightRepository.findWithFilters(insightType, competitor, PageRequest.of(page, size))
                .map(InsightDto.Response::from);
    }

    // 인사이트 상세 조회
    @Transactional(readOnly = true)
    public InsightDto.DetailResponse getInsight(Long id) {
        Insight insight = insightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("인사이트를 찾을 수 없습니다: " + id));
        return InsightDto.DetailResponse.from(insight);
    }

    // 오늘 인사이트 조회
    @Transactional(readOnly = true)
    public List<InsightDto.Response> getTodayInsights() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return insightRepository.findByGeneratedAtAfterOrderByImpactScoreDesc(startOfDay)
                .stream()
                .map(InsightDto.Response::from)
                .collect(Collectors.toList());
    }

    // 수동 인사이트 생성 트리거
    @Transactional
    public List<InsightDto.Response> generateInsights() {
        log.info("수동 인사이트 생성 시작");
        return generateFromRecentArticles();
    }

    // 미처리 기사 기반 인사이트 생성
    private List<InsightDto.Response> generateFromRecentArticles() {
        List<Article> articles = articleService.getUnprocessedArticles();
        if (articles.isEmpty()) {
            log.info("처리할 기사가 없습니다.");
            return List.of();
        }

        log.info("{}건 기사로 인사이트 생성 중...", articles.size());
        List<Insight> insights = insightGenerator.generate(articles);

        List<Insight> saved = insights.stream()
                .map(insightRepository::save)
                .collect(Collectors.toList());

        // 처리된 기사 마킹
        articles.forEach(article -> articleService.markAsProcessed(article.getId()));

        log.info("인사이트 {}건 생성 완료", saved.size());
        return saved.stream().map(InsightDto.Response::from).collect(Collectors.toList());
    }

    // 고영향도 인사이트 수
    @Transactional(readOnly = true)
    public long getHighImpactCount() {
        return insightRepository.countByImpactScoreGreaterThanEqual(4);
    }

    // 미처리 인사이트 수
    @Transactional(readOnly = true)
    public long getUnprocessedInsightCount() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        return insightRepository.findByGeneratedAtAfterOrderByImpactScoreDesc(since).size();
    }

    // 최근 인사이트 3건 (대시보드용)
    @Transactional(readOnly = true)
    public List<InsightDto.Response> getLatestInsights() {
        return insightRepository.findTop3ByOrderByGeneratedAtDesc()
                .stream()
                .map(InsightDto.Response::from)
                .collect(Collectors.toList());
    }
}
