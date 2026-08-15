package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.crawler.CrawlerOrchestrator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.RadarDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RadarCollectionService {

    private static final int MAX_ANALYSIS_PER_RUN = 12;

    private final CrawlerOrchestrator crawlerOrchestrator;
    private final ArticleRepository articleRepository;
    private final RadarPlayerRepository radarPlayerRepository;
    private final RadarSignalRepository radarSignalRepository;
    private final RadarSignalAnalyzer radarSignalAnalyzer;
    private final RadarService radarService;

    public CollectionResult collect() {
        LocalDateTime startedAt = LocalDateTime.now();
        int collectedArticleCount = crawlerOrchestrator.crawlAll();
        List<RadarPlayer> watchlist = radarPlayerRepository.findByActiveTrueOrderByLayerAscWatchPriorityAscNameAsc();
        List<Article> candidates = articleRepository.findByCollectedAtBetweenOrderByCollectedAtDesc(startedAt, LocalDateTime.now())
                .stream()
                .filter(article -> radarSignalRepository.findBySourceUrl(article.getUrl()).isEmpty())
                .limit(MAX_ANALYSIS_PER_RUN)
                .toList();

        int savedSignalCount = 0;
        for (Article article : candidates) {
            RadarSignalAnalyzer.AnalysisResult analysis = radarSignalAnalyzer.analyze(article, watchlist);
            if (analysis == null || analysis.fact().isBlank() || analysis.whatChanged().isBlank()
                    || analysis.industryStructureImpact().isBlank() || analysis.recommendedAction().isBlank()) {
                continue;
            }
            try {
                radarService.registerSignal(new RadarDto.SignalRequest(
                        article.getTitle(), analysis.fact(), article.getUrl(), sourceTier(article), analysis.signalType(),
                        article.getPublishedAt() == null ? article.getCollectedAt() : article.getPublishedAt(),
                        analysis.confidenceScore(), analysis.impactScore(), analysis.lenses(), analysis.playerNames(),
                        new RadarDto.AssessmentRequest(analysis.whatChanged(), analysis.industryStructureImpact(),
                                analysis.mspOpportunity(), analysis.mspThreat(), analysis.structuralRisk(),
                                analysis.recommendedAction(), null, null)
                ));
                savedSignalCount++;
            } catch (IllegalArgumentException ignored) {
                // 중복 URL, 유효하지 않은 Watch List 등은 해당 기사만 건너뛴다.
            }
        }
        return new CollectionResult(collectedArticleCount, candidates.size(), savedSignalCount);
    }

    private String sourceTier(Article article) {
        return "HOMEPAGE".equals(article.getSourceType()) ? "TIER_1" : "TIER_2";
    }

    public record CollectionResult(int collectedArticleCount, int analyzedArticleCount, int savedSignalCount) {
    }
}
