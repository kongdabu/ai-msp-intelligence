package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.ai.GeminiWorkCoordinator;
import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleAnalysisService;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.RadarDto;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CancellationException;

@Service
@Slf4j
@RequiredArgsConstructor
public class RadarCollectionService {

    private final ArticleRepository articleRepository;
    private final RadarPlayerRepository radarPlayerRepository;
    private final RadarSignalRepository radarSignalRepository;
    private final RadarSignalAnalyzer radarSignalAnalyzer;
    private final RadarService radarService;
    private final GeminiWorkCoordinator geminiWorkCoordinator;
    private final AppConfig appConfig;

    public CollectionResult collect() {
        return geminiWorkCoordinator.executeExclusive("Radar Signal 분석", this::collectInternal);
    }

    private CollectionResult collectInternal() {
        List<RadarPlayer> watchlist = radarPlayerRepository.findByActiveTrueOrderByLayerAscWatchPriorityAscNameAsc();
        List<Article> candidates = buildCandidates(watchlist);

        int savedSignalCount = 0;
        for (Article article : candidates) {
            if (Thread.currentThread().isInterrupted()) throw new CancellationException("Radar 수집 작업이 취소되었습니다.");
            try {
                RadarSignalAnalyzer.AnalysisResult analysis = radarSignalAnalyzer.analyze(article, watchlist);
                article.setRadarAnalyzedAt(LocalDateTime.now());
                if (!analysis.relevant() || analysis.fact().isBlank() || analysis.whatChanged().isBlank()
                        || analysis.industryStructureImpact().isBlank() || analysis.recommendedAction().isBlank()) {
                    article.setRadarAnalysisStatus("IRRELEVANT");
                    articleRepository.save(article);
                    continue;
                }
                radarService.registerSignal(new RadarDto.SignalRequest(
                        article.getTitle(), analysis.fact(), article.getUrl(), sourceTier(article), analysis.signalType(),
                        article.getPublishedAt() == null ? article.getCollectedAt() : article.getPublishedAt(),
                        analysis.confidenceScore(), analysis.impactScore(), analysis.lenses(), analysis.playerNames(),
                        new RadarDto.AssessmentRequest(analysis.whatChanged(), analysis.industryStructureImpact(),
                                analysis.mspOpportunity(), analysis.mspThreat(), analysis.structuralRisk(),
                                analysis.recommendedAction(), null, null)
                ));
                article.setRadarAnalysisStatus("COMPLETED");
                articleRepository.save(article);
                savedSignalCount++;
            } catch (AiApiUnavailableException e) {
                article.setRadarAnalysisStatus("PENDING");
                articleRepository.save(article);
                log.warn("Gemini 호출 제한으로 Radar 분석을 중단하고 다음 배치로 넘깁니다.");
                break;
            } catch (IllegalArgumentException e) {
                article.setRadarAnalysisStatus("IRRELEVANT");
                article.setRadarAnalyzedAt(LocalDateTime.now());
                articleRepository.save(article);
            }
        }
        return new CollectionResult(0, candidates.size(), savedSignalCount);
    }

    private static final List<String> PRICING_KEYWORDS = List.of(
            "pricing", "price", "monetization", "요금", "가격", "과금", "토큰 단가", "토큰 비용", "비용 절감", "할인", "캐싱"
    );

    private List<Article> buildCandidates(List<RadarPlayer> watchlist) {
        List<Article> pendingArticles = articleRepository.findByAnalysisStatusAndRadarAnalysisStatusOrderByCollectedAtDesc(
                ArticleAnalysisService.COMPLETED, "PENDING", PageRequest.of(0, 100)).getContent();
        Set<String> existingSourceUrls = radarSignalRepository.findBySourceUrlIn(pendingArticles.stream().map(Article::getUrl).toList()).stream()
                .map(RadarSignal::getSourceUrl)
                .collect(java.util.stream.Collectors.toSet());
        return pendingArticles.stream()
                .peek(article -> {
                    if (!isWatchlistTarget(article, watchlist)) {
                        article.setRadarAnalysisStatus("NOT_TARGET");
                        article.setRadarAnalyzedAt(LocalDateTime.now());
                        articleRepository.save(article);
                    } else if (existingSourceUrls.contains(article.getUrl())) {
                        article.setRadarAnalysisStatus("COMPLETED");
                        article.setRadarAnalyzedAt(LocalDateTime.now());
                        articleRepository.save(article);
                    }
                })
                .filter(article -> "PENDING".equals(article.getRadarAnalysisStatus()))
                .sorted(java.util.Comparator.comparingInt(this::candidatePriority).reversed()
                        .thenComparing(Article::getCollectedAt, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .limit(appConfig.getRadarAnalysisPerRun())
                .toList();
    }

    private int candidatePriority(Article article) {
        int score = 0;
        String text = ((article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getSummary() == null ? "" : article.getSummary())).toLowerCase(Locale.ROOT);
        if (PRICING_KEYWORDS.stream().anyMatch(text::contains)) {
            score += 100;
        }
        if ("HOMEPAGE".equals(article.getSourceType())) {
            score += 50;
        }
        return score;
    }

    private boolean isWatchlistTarget(Article article, List<RadarPlayer> watchlist) {
        String text = ((article.getSourceName() == null ? "" : article.getSourceName()) + " "
                + (article.getUrl() == null ? "" : article.getUrl()) + " "
                + (article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getSummary() == null ? "" : article.getSummary())).toLowerCase(Locale.ROOT);
        return watchlist.stream().anyMatch(player -> matchesPlayer(text, player));
    }

    private boolean matchesPlayer(String text, RadarPlayer player) {
        String normalizedName = player.getName().toLowerCase(Locale.ROOT);
        if (text.contains(normalizedName)) return true;
        String domain = domainOf(player.getWebsite());
        return domain != null && text.contains(domain);
    }

    private String domainOf(String website) {
        if (website == null || website.isBlank()) return null;
        try {
            String host = URI.create(website).getHost();
            if (host == null) return null;
            return host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String sourceTier(Article article) {
        return "HOMEPAGE".equals(article.getSourceType()) ? "TIER_1" : "TIER_2";
    }

    public record CollectionResult(int collectedArticleCount, int analyzedArticleCount, int savedSignalCount) {
    }
}
