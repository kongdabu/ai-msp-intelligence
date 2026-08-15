package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.ai.SummaryGenerator;
import com.aimsp.intelligence.crawler.sources.AiEcosystemCrawler;
import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerOrchestrator {

    private static final List<String> TARGET_KEYWORDS = List.of(
            "frontier ai", "openai", "anthropic", "gemini", "aws", "microsoft", "엔비디아", "nvidia",
            "accenture", "딜로이트", "deloitte", "pwc", "fde", "rde", "ode", "partnership", "파트너십",
            "agentic ai", "agentic", "aiops", "ai ops", "forward deployed", "resident deployed",
            "ai pricing model", "ai pricing", "usage-based pricing", "outcome-based pricing", "value-based pricing", "ai monetization",
            "ai 인력 양성", "ai 인재", "ai 교육", "ai 리스킬링", "ai workforce", "ai talent", "ai training", "ai upskilling", "ai reskilling",
            "ai 価格モデル", "ai 人材育成", "ai リスキリング", "生成ai 人材育成"
    );

    private final ArticleService articleService;
    private final SummaryGenerator summaryGenerator;
    private final GeminiApiClient geminiApiClient;
    private final AiEcosystemCrawler aiEcosystemCrawler;
    private final OfficialSiteCrawler officialSiteCrawler;
    private final AppConfig appConfig;

    private final ExecutorService crawlerPool = Executors.newFixedThreadPool(3);

    @PreDestroy
    public void shutdown() {
        crawlerPool.shutdown();
        try {
            if (!crawlerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                crawlerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            crawlerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("null")
    public int crawlAll() {
        if (!geminiApiClient.isAvailable()) {
            throw new AiApiUnavailableException();
        }

        int totalSaved = 0;

        totalSaved += crawlAndSave(officialSiteCrawler.crawl(), "공식 사이트", true);
        totalSaved += crawlAndSave(aiEcosystemCrawler.crawl(), "AI 생태계·사업모델 뉴스", false);

        log.info("=== 크롤링 완료: 총 {}건 저장 ===", totalSaved);
        return totalSaved;
    }

    private int crawlAndSave(List<Article> articles, String sourceName, boolean officialSource) {
        int saved = 0;
        int skipped = 0;
        int preFiltered = 0;
        for (Article article : articles) {
            try {
                if (!officialSource && !matchesTargetKeyword(article)) {
                    preFiltered++;
                    continue;
                }
                if (articleService.existsByUrl(article.getUrl())) {
                    skipped++;
                    continue;
                }

                if (article.getOriginalContent() != null && !article.getOriginalContent().isBlank()) {
                    SummaryGenerator.SummaryResult result = summaryGenerator.generateSummary(
                            article.getTitle(), article.getOriginalContent()
                    );
                    if (result == null && officialSource) {
                        skipped++;
                        continue;
                    }
                    if (result != null) {
                        int minimumRelevanceScore = officialSource
                                ? appConfig.getOfficialSiteMinimumRelevanceScore()
                                : 50;
                        if (result.relevanceScore() < minimumRelevanceScore) {
                            log.debug("관련도 미달 기사 제외 [score={}]: {}", result.relevanceScore(), article.getTitle());
                            skipped++;
                            continue;
                        }
                        article.setSummary(result.summary());
                        article.setRelevanceScore(result.relevanceScore());
                        if (article.getCategory() == null) {
                            article.setCategory(result.detectedCategory());
                        }
                    }
                } else if (officialSource) {
                    skipped++;
                    continue;
                }

                Article savedArticle = articleService.saveIfNotExists(article);
                if (savedArticle != null) saved++;
            } catch (AiApiUnavailableException e) {
                throw e;
            } catch (Exception e) {
                log.error("기사 저장 실패 [{}]: {}", article.getTitle(), e.getMessage());
            }
        }
        log.info("[{}] 신규 {}건 저장, 사전 필터 {}건·중복 {}건 제외", sourceName, saved, preFiltered, skipped);
        return saved;
    }

    private boolean matchesTargetKeyword(Article article) {
        String text = ((article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getOriginalContent() == null ? "" : article.getOriginalContent()))
                .toLowerCase(Locale.ROOT);
        return TARGET_KEYWORDS.stream().anyMatch(text::contains);
    }

    private List<Article> safeGet(CompletableFuture<List<Article>> future, String name) {
        try {
            return future.get();
        } catch (Exception e) {
            log.error("[{}] 크롤러 실패: {}", name, e.getMessage());
            return List.of();
        }
    }
}
