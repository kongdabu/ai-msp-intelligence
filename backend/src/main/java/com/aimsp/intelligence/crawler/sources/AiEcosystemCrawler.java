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
public class AiEcosystemCrawler {
    private static final List<String> QUERIES = List.of(
            "Frontier AI Labs", "OpenAI", "Anthropic", "Gemini AI", "AWS AI", "Microsoft AI", "엔비디아 AI",
            "Accenture AI", "딜로이트 AI", "PwC AI", "AI FDE", "AI RDE", "AI ODE", "AI Partnership", "Agentic AI", "AIOps",
            "AI Pricing Model", "AI monetization", "AI 요금제", "AI 가격 인하", "AI API 가격", "AI 과금",
            "AI 인력 양성", "AI 人材育成", "AI リスキリング"
    );
    private final NaverNewsClient naverNewsClient;

    public List<Article> crawl() {
        Map<String, Article> articlesByUrl = new LinkedHashMap<>();
        for (String query : QUERIES) {
            try {
                for (NaverNewsItem item : naverNewsClient.search(query)) {
                    String url = item.bestUrl();
                    if (url == null || url.isBlank() || articlesByUrl.containsKey(url)) continue;
                    Article article = new Article();
                    article.setUrl(url);
                    article.setTitle(item.cleanTitle());
                    article.setOriginalContent(item.cleanDescription());
                    article.setSourceName("AI 생태계·사업모델 뉴스");
                    article.setSourceType("NEWS");
                    article.setCompetitor("GENERAL");
                    article.setPublishedAt(item.parsedDate());
                    articlesByUrl.put(url, article);
                }
            } catch (Exception e) {
                log.error("AI 생태계 뉴스 수집 실패 [{}]: {}", query, e.getMessage());
            }
        }
        return new ArrayList<>(articlesByUrl.values());
    }
}
