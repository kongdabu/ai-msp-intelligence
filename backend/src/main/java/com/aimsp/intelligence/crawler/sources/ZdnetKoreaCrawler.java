package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.crawler.NaverNewsClient;
import com.aimsp.intelligence.crawler.NaverNewsClient.NaverNewsItem;
import com.aimsp.intelligence.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZdnetKoreaCrawler {

    private static final String QUERY = "AI MSP";
    private static final String SOURCE_NAME = "AI MSP 업계 뉴스";
    private static final String COMPETITOR = "GENERAL";

    private final NaverNewsClient naverNewsClient;

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();
        try {
            List<NaverNewsItem> items = naverNewsClient.search(QUERY);
            for (NaverNewsItem item : items) {
                Article article = new Article();
                article.setUrl(item.bestUrl());
                article.setTitle(item.cleanTitle());
                article.setOriginalContent(item.cleanDescription());
                article.setSourceName(SOURCE_NAME);
                article.setSourceType("NEWS");
                article.setCompetitor(detectCompetitor(item.cleanTitle()));
                article.setPublishedAt(item.parsedDate());
                articles.add(article);
            }
            log.info("AI MSP 업계 뉴스 수집 완료: {}건", articles.size());
        } catch (Exception e) {
            log.error("AI MSP 업계 뉴스 수집 실패: {}", e.getMessage());
        }
        return articles;
    }

    private String detectCompetitor(String title) {
        if (title == null) return COMPETITOR;
        if (title.contains("LG CNS") || title.contains("LGCNS")) return "LG_CNS";
        if (title.contains("SK AX") || title.contains("SK에이엔에스")) return "SK_AX";
        if (title.contains("베스핀") || title.contains("Bespin")) return "BESPIN";
        if (title.contains("PwC") || title.contains("삼일회계") || title.contains("삼일PwC")) return "PWC";
        return COMPETITOR;
    }
}
