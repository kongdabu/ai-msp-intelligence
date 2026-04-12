package com.aimsp.intelligence.crawler;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerOrchestrator {

    private final ArticleService articleService;
    private final SourceService sourceService;
    private final SummaryGenerator summaryGenerator;
    private final RssCrawler rssCrawler;
    private final LgCnsCrawler lgCnsCrawler;
    private final SkAxCrawler skAxCrawler;
    private final BespinCrawler bespinCrawler;
    private final PwcCrawler pwcCrawler;
    private final ZdnetKoreaCrawler zdnetKoreaCrawler;

    private static final long REQUEST_DELAY_MS = 2000L;

    /**
     * 전체 소스 크롤링 - 뉴스 기사 전용 (Google News RSS + 일반 뉴스 RSS)
     */
    public int crawlAll() {
        int totalSaved = 0;

        // 1. 경쟁사별 Google News RSS 크롤러
        log.info("--- 경쟁사 뉴스 수집 시작 ---");
        totalSaved += crawlAndSave(lgCnsCrawler.crawl(), "LG_CNS");
        sleep();
        totalSaved += crawlAndSave(skAxCrawler.crawl(), "SK_AX");
        sleep();
        totalSaved += crawlAndSave(bespinCrawler.crawl(), "BESPIN");
        sleep();
        totalSaved += crawlAndSave(pwcCrawler.crawl(), "PWC");
        sleep();
        totalSaved += crawlAndSave(zdnetKoreaCrawler.crawl(), "GENERAL");

        // 2. 소스 DB의 활성 뉴스 RSS 소스 크롤링 (NEWS 타입만)
        log.info("--- 뉴스 RSS 소스 수집 시작 ---");
        List<Source> activeSources = sourceService.getActiveSources();
        for (Source source : activeSources) {
            if (!"NEWS".equals(source.getType())) continue; // 뉴스 소스만
            try {
                sleep();
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
     * 기사 목록 AI 요약 처리 후 저장 (요약 실패해도 원문 저장)
     */
    private int crawlAndSave(List<Article> articles, String sourceName) {
        int saved = 0;
        for (Article article : articles) {
            try {
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
                sleep();
            } catch (Exception e) {
                log.error("기사 저장 실패 [{}]: {}", article.getTitle(), e.getMessage());
            }
        }
        log.info("[{}] {}건 신규 저장", sourceName, saved);
        return saved;
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(REQUEST_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
