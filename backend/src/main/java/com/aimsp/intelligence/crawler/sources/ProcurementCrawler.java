package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.crawler.ProcurementApiClient;
import com.aimsp.intelligence.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcurementCrawler {

    private static final List<String> KEYWORDS = List.of("AI", "인공지능", "클라우드", "MSP");
    private static final int ITEMS_PER_KEYWORD = 10;

    private final ProcurementApiClient procurementApiClient;

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();
        Set<String> seenUrls = new java.util.HashSet<>();

        for (String keyword : KEYWORDS) {
            List<ProcurementApiClient.ProcurementItem> items =
                    procurementApiClient.search(keyword, ITEMS_PER_KEYWORD);

            for (ProcurementApiClient.ProcurementItem item : items) {
                if (!seenUrls.add(item.url())) continue; // URL 기준 중복 제거

                Article article = new Article();
                article.setTitle(item.title());
                article.setUrl(item.url());
                article.setOriginalContent(buildContent(item));
                article.setSourceName("나라장터");
                article.setSourceType("PROCUREMENT");
                article.setCompetitor("GENERAL");
                article.setPublishedAt(item.publishedAt());
                article.setCollectedAt(LocalDateTime.now());
                articles.add(article);
            }
        }

        log.info("[나라장터] 수집 완료: {}건 (중복 제거 후)", articles.size());
        return articles;
    }

    private String buildContent(ProcurementApiClient.ProcurementItem item) {
        StringBuilder sb = new StringBuilder();
        if (!item.institutionName().isBlank()) sb.append("발주기관: ").append(item.institutionName()).append(" | ");
        if (!item.bidDate().isBlank())         sb.append("공고일: ").append(item.bidDate()).append(" | ");
        if (!item.budgetAmount().isBlank())    sb.append("예산: ").append(item.budgetAmount()).append("원");
        return sb.toString();
    }
}
