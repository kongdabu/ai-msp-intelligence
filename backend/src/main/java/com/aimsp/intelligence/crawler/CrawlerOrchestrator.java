package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CancellationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerOrchestrator {

    private final ArticleService articleService;
    private final OfficialSiteCrawler officialSiteCrawler;

    public int crawlAll() {
        int totalSaved = crawlAndSave(officialSiteCrawler.crawl(), "공식 사이트");

        log.info("=== 크롤링 완료: 총 {}건 저장 ===", totalSaved);
        return totalSaved;
    }

    private int crawlAndSave(List<Article> articles, String sourceName) {
        int saved = 0;
        int skipped = 0;
        for (Article article : articles) {
            try {
                if (Thread.currentThread().isInterrupted()) throw new CancellationException("수집 작업이 취소되었습니다.");
                if (articleService.existsByUrl(article.getUrl())) {
                    skipped++;
                    continue;
                }

                if (article.getOriginalContent() == null || article.getOriginalContent().isBlank()) {
                    skipped++;
                    continue;
                }

                Article savedArticle = articleService.saveIfNotExists(article);
                if (savedArticle != null) saved++;
            } catch (CancellationException e) {
                throw e;
            } catch (Exception e) {
                log.error("기사 저장 실패 [{}]: {}", article.getTitle(), e.getMessage());
            }
        }
        log.info("[{}] 신규 {}건 저장, 중복 또는 본문 없음 {}건 제외", sourceName, saved, skipped);
        return saved;
    }

}
