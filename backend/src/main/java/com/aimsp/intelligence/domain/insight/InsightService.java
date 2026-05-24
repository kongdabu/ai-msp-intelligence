package com.aimsp.intelligence.domain.insight;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.ai.InsightGenerator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import com.aimsp.intelligence.dto.InsightDto;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final ArticleService articleService;
    private final InsightGenerator insightGenerator;
    private final GeminiApiClient geminiApiClient;
    private final PlatformTransactionManager transactionManager;

    // 인사이트 목록 조회
    // Specification 사용 - null 조건은 쿼리에서 제외하여 PostgreSQL 타입 추론 오류 방지
    @Transactional(readOnly = true)
    public Page<InsightDto.Response> getInsights(String insightType, String competitor, int page, int size) {
        Specification<Insight> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (insightType != null) predicates.add(cb.equal(root.get("insightType"), insightType));
            if (competitor != null)  predicates.add(cb.equal(root.get("competitor"), competitor));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return insightRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "generatedAt")))
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
    public List<InsightDto.Response> generateInsights() {
        if (!geminiApiClient.isAvailable()) {
            throw new AiApiUnavailableException();
        }
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

        // API 호출 완료 후, DB 저장 및 마킹만 개별 트랜잭션으로 처리 (Connection starvation 방지)
        // DTO 변환도 트랜잭션 내부에서 수행 — 세션 종료 후 지연 로딩 컬렉션 접근 방지
        List<InsightDto.Response> savedDtos = new TransactionTemplate(transactionManager).execute(status -> {
            List<InsightDto.Response> dtoList = insights.stream()
                    .map(insight -> {
                        insight.getSourceArticles().forEach(ia -> ia.setInsight(insight));
                        return InsightDto.Response.from(insightRepository.save(insight));
                    })
                    .collect(Collectors.toList());

            // 인사이트가 실제로 생성된 경우에만 기사를 처리 완료로 마킹
            if (!dtoList.isEmpty()) {
                articles.forEach(article -> articleService.markAsProcessed(article.getId()));
            }
            return dtoList;
        });

        log.info("인사이트 {}건 생성 완료", savedDtos != null ? savedDtos.size() : 0);
        return savedDtos != null ? savedDtos : List.of();
    }

    // 고영향도 인사이트 수
    @Transactional(readOnly = true)
    public long getHighImpactCount() {
        return insightRepository.countByImpactScoreGreaterThanEqual(4);
    }

    // 최근 24시간 인사이트 수 (대시보드 KPI - COUNT 쿼리 사용)
    @Transactional(readOnly = true)
    public long getUnprocessedInsightCount() {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        return insightRepository.countByGeneratedAtAfter(since);
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
