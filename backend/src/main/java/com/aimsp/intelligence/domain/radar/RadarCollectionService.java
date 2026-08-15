package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.crawler.CrawlerOrchestrator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.RadarDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        List<Article> candidates = buildCandidates(startedAt, watchlist);

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

    private List<Article> buildCandidates(LocalDateTime startedAt, List<RadarPlayer> watchlist) {
        Map<String, Article> candidatesByUrl = new LinkedHashMap<>();
        articleRepository.findByCollectedAtBetweenOrderByCollectedAtDesc(startedAt, LocalDateTime.now())
                .forEach(article -> candidatesByUrl.put(article.getUrl(), article));
        // Watch List에 새 사업자가 추가되면 기존에 수집한 해당 사업자의 원문도 재분석한다.
        articleRepository.findTop200ByOrderByCollectedAtDesc().stream()
                .filter(article -> isWatchlistSource(article, watchlist))
                .forEach(article -> candidatesByUrl.putIfAbsent(article.getUrl(), article));

        return candidatesByUrl.values().stream()
                .filter(article -> radarSignalRepository.findBySourceUrl(article.getUrl()).isEmpty())
                .limit(MAX_ANALYSIS_PER_RUN)
                .toList();
    }

    private boolean isWatchlistSource(Article article, List<RadarPlayer> watchlist) {
        String source = ((article.getSourceName() == null ? "" : article.getSourceName()) + " "
                + (article.getUrl() == null ? "" : article.getUrl())).toLowerCase(Locale.ROOT);
        return watchlist.stream()
                .map(RadarPlayer::getName)
                .map(name -> name.toLowerCase(Locale.ROOT).split("\\s+")[0])
                .anyMatch(source::contains);
    }

    private String sourceTier(Article article) {
        return "HOMEPAGE".equals(article.getSourceType()) ? "TIER_1" : "TIER_2";
    }

    public record CollectionResult(int collectedArticleCount, int analyzedArticleCount, int savedSignalCount) {
    }
}
