package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.crawler.NaverNewsClient;
import com.aimsp.intelligence.crawler.NaverNewsClient.NaverNewsItem;
import com.aimsp.intelligence.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZdnetKoreaCrawler {

    // Google News Sources(AI Agent, 클라우드 MSP, 금융 AI)를 비활성화하고
    // 동일 키워드를 Naver API로 커버
    private static final List<String> QUERIES = List.of(
            "AI MSP",
            "AI 에이전트",
            "클라우드 MSP",
            "금융 AI",
            "AI ITO"
    );
    private static final String SOURCE_NAME = "AI MSP 업계 뉴스";
    private static final String COMPETITOR = "GENERAL";

    private final NaverNewsClient naverNewsClient;

    public List<Article> crawl() {
        // URL 기준 중복 제거 (LinkedHashMap으로 순서 유지)
        Map<String, Article> seen = new LinkedHashMap<>();
        for (String query : QUERIES) {
            try {
                List<NaverNewsItem> items = naverNewsClient.search(query);
                for (NaverNewsItem item : items) {
                    String url = item.bestUrl();
                    if (url == null || url.isBlank() || seen.containsKey(url)) continue;
                    Article article = new Article();
                    article.setUrl(url);
                    article.setTitle(item.cleanTitle());
                    article.setOriginalContent(item.cleanDescription());
                    article.setSourceName(SOURCE_NAME);
                    article.setSourceType("NEWS");
                    article.setCompetitor(detectCompetitor(item.cleanTitle()));
                    article.setPublishedAt(item.parsedDate());
                    seen.put(url, article);
                }
            } catch (Exception e) {
                log.error("AI MSP 업계 뉴스 수집 실패 [{}]: {}", query, e.getMessage());
            }
        }
        List<Article> articles = new ArrayList<>(seen.values());
        log.info("AI MSP 업계 뉴스 수집 완료: {}건 ({}개 쿼리)", articles.size(), QUERIES.size());
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
