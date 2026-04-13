package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.ai.SummaryGenerator;
import com.aimsp.intelligence.crawler.sources.BespinCrawler;
import com.aimsp.intelligence.crawler.sources.LgCnsCrawler;
import com.aimsp.intelligence.crawler.sources.PwcCrawler;
import com.aimsp.intelligence.crawler.sources.SkAxCrawler;
import com.aimsp.intelligence.crawler.sources.ZdnetKoreaCrawler;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import com.aimsp.intelligence.domain.source.Source;
import com.aimsp.intelligence.domain.source.SourceService;
import com.aimsp.intelligence.exception.GeminiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerOrchestrator {

    private final ArticleService articleService;
    private final SourceService sourceService;
    private final SummaryGenerator summaryGenerator;
    private final GeminiApiClient geminiApiClient;
    private final RssCrawler rssCrawler;
    private final LgCnsCrawler lgCnsCrawler;
    private final SkAxCrawler skAxCrawler;
    private final BespinCrawler bespinCrawler;
    private final PwcCrawler pwcCrawler;
    private final ZdnetKoreaCrawler zdnetKoreaCrawler;

    // 크롤러 병렬 실행 스레드풀 (Google News 동시 요청 수 제한)
    private static final ExecutorService CRAWLER_POOL = Executors.newFixedThreadPool(3);

    /**
     * 전체 소스 크롤링 - 5개 전용 크롤러 병렬 실행 후 RSS 소스 수집
     * 시작 전 Gemini API 헬스체크 수행 - 비정상 시 GeminiApiUnavailableException 발생
     */
    public int crawlAll() {
        if (!geminiApiClient.isAvailable()) {
            throw new GeminiApiUnavailableException();
        }

        int totalSaved = 0;

        // 1. 경쟁사별 Google News RSS 크롤러 병렬 실행
        log.info("--- 경쟁사 뉴스 수집 시작 (병렬) ---");
        CompletableFuture<List<Article>> lgFuture   = CompletableFuture.supplyAsync(lgCnsCrawler::crawl, CRAWLER_POOL);
        CompletableFuture<List<Article>> skFuture   = CompletableFuture.supplyAsync(skAxCrawler::crawl, CRAWLER_POOL);
        CompletableFuture<List<Article>> bespinFuture = CompletableFuture.supplyAsync(bespinCrawler::crawl, CRAWLER_POOL);
        CompletableFuture<List<Article>> pwcFuture  = CompletableFuture.supplyAsync(pwcCrawler::crawl, CRAWLER_POOL);
        CompletableFuture<List<Article>> zdnetFuture = CompletableFuture.supplyAsync(zdnetKoreaCrawler::crawl, CRAWLER_POOL);

        CompletableFuture.allOf(lgFuture, skFuture, bespinFuture, pwcFuture, zdnetFuture).join();

        List<Article> competitorArticles = new ArrayList<>();
        competitorArticles.addAll(safeGet(lgFuture,    "LG_CNS"));
        competitorArticles.addAll(safeGet(skFuture,    "SK_AX"));
        competitorArticles.addAll(safeGet(bespinFuture,"BESPIN"));
        competitorArticles.addAll(safeGet(pwcFuture,   "PWC"));
        competitorArticles.addAll(safeGet(zdnetFuture, "GENERAL"));

        totalSaved += crawlAndSave(competitorArticles, "경쟁사 뉴스");

        // 2. 소스 DB의 활성 뉴스 RSS 소스 크롤링 (NEWS 타입만)
        log.info("--- 뉴스 RSS 소스 수집 시작 ---");
        List<Source> activeSources = sourceService.getActiveSources();
        for (Source source : activeSources) {
            if (!"NEWS".equals(source.getType())) continue;
            try {
                List<Article> articles = rssCrawler.crawl(source);
                int saved = crawlAndSave(articles, source.getName());
                totalSaved += saved;
                sourceService.updateLastCrawled(source.getId());
            } catch (Exception e) {
                log.error("RSS 소스 크롤링 실패 [{}]: {}", source.getName(), e.getMessage());
                sourceService.incrementErrorCount(source.getId());
            }
        }

        log.info("=== 크롤링 완료: 총 {}건 저장 ===", totalSaved);
        return totalSaved;
    }

    /**
     * 기사 목록 저장 - 중복 URL은 Gemini 호출 없이 즉시 스킵 (API 호출 최소화)
     */
    private int crawlAndSave(List<Article> articles, String sourceName) {
        int saved = 0;
        int skipped = 0;
        for (Article article : articles) {
            try {
                // 중복 체크를 Gemini 호출 전에 수행 → 신규 기사에만 AI 요약 생성
                if (articleService.existsByUrl(article.getUrl())) {
                    skipped++;
                    continue;
                }

                if (article.getOriginalContent() != null && !article.getOriginalContent().isBlank()) {
                    SummaryGenerator.SummaryResult result = summaryGenerator.generateSummary(
                            article.getTitle(), article.getOriginalContent()
                    );
                    if (result != null) {
                        article.setSummary(result.summary());
                        article.setRelevanceScore(result.relevanceScore());
                        if ("GENERAL".equals(article.getCompetitor())) {
                            article.setCompetitor(result.detectedCompetitor());
                        }
                        if (article.getCategory() == null) {
                            article.setCategory(result.detectedCategory());
                        }
                    }
                }

                Article savedArticle = articleService.saveIfNotExists(article);
                if (savedArticle != null) saved++;
            } catch (Exception e) {
                log.error("기사 저장 실패 [{}]: {}", article.getTitle(), e.getMessage());
            }
        }
        log.info("[{}] 신규 {}건 저장, 중복 {}건 스킵", sourceName, saved, skipped);
        return saved;
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
