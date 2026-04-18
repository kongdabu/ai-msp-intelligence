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
public class LgCnsCrawler {

    private static final String QUERY = "LG CNS";
    private static final String SOURCE_NAME = "LG CNS 뉴스";
    private static final String COMPETITOR = "LG_CNS";

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
                article.setCompetitor(COMPETITOR);
                article.setPublishedAt(item.parsedDate());
                articles.add(article);
            }
            log.info("LG CNS 뉴스 수집 완료: {}건", articles.size());
        } catch (Exception e) {
            log.error("LG CNS 뉴스 수집 실패: {}", e.getMessage());
        }
        return articles;
    }
}
