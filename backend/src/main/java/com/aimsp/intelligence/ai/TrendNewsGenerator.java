package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.config.SystemConfigService;
import com.aimsp.intelligence.domain.trend.TrendNews;
import com.aimsp.intelligence.domain.trend.TrendNewsArticle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrendNewsGenerator {

    private static final int MAX_ARTICLES_FOR_TREND = 120;
    private static final int MAX_TRENDS = 3;
    private static final int MIN_SOURCE_ARTICLES = 3;

    private static final String PROMPT_TEMPLATE = """
            너는 한국 금융·공공 엔터프라이즈 시장을 담당하는 AI MSP 전략 에디터다.
            아래 최근 30일간 수집된 기사를 비교 분석해, 여러 기사에서 공통적으로 반복·확산되는 Hot Trend Top 3를 작성해줘.

            [중요 원칙]
            - 단일 기업의 일회성 발표가 아니라, 최소 3개 기사로 확인되는 공통 흐름만 선정한다.
            - Top 3는 서로 다른 주제여야 하며, 제목·핵심 주장·근거 기사를 중복해서 사용하지 않는다.
            - 기사 수, 경쟁사·출처 다양성, 최근성, AI MSP/Agentic ITO 사업 관련도를 종합해 hotScore(0-100)를 매긴다.
            - 카테고리 이름을 그대로 반복하지 말고, 실제 사업 변화가 드러나는 구체적인 주제로 묶는다.
            - 확인 가능한 사실과 전략적 해석을 구분한다. 기사에 없는 숫자·고객사·성과를 만들지 않는다.
            - 금융 규제, 공공 보안, AI Agent 기반 ITO 전환 관점을 반영한다.
            - 각 본문은 블로그 뉴스 형식으로 700~1,300자 내외로 쓴다. 문단은 '핵심 변화', '관찰된 신호', '사업 시사점' 순서로 구성한다.
            - 본문에서 근거를 언급할 때는 반드시 [기사 #ID] 형태로 표시한다.
            - 각 트렌드는 sourceArticles에 실제 근거 기사 3~8건을 연결한다. 유사도 65점 미만 기사는 제외한다.

            [출력 형식]
            JSON만 출력한다.
            {"trends":[{"title":"제목 45자 이내","summary":"한 줄 요약 180자 이내","content":"블로그 본문","hotScore":86,"confidence":82,"keywords":["키워드1","키워드2"],"actionItems":["검토할 액션 1","검토할 액션 2"],"sourceArticles":[{"id":1,"relevance":88}]}]}

            [분석 기사]
            %s
            """;

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final SystemConfigService systemConfigService;

    public List<TrendNews> generate(List<Article> articles, LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (articles == null || articles.size() < MIN_SOURCE_ARTICLES) return List.of();

        List<Article> sample = selectEvenlyDistributedSample(articles);
        Map<Long, Article> articleById = new LinkedHashMap<>();
        for (Article article : sample) articleById.put(article.getId(), article);

        try {
            String prompt = String.format(PROMPT_TEMPLATE, buildArticlesJson(sample));
            String response = geminiApiClient.call(prompt);
            if (response == null) return List.of();

            int minRelevance = systemConfigService.getOrCreate().getMinRelevanceScoreForInsight();
            JsonNode trendNodes = objectMapper.readTree(response).path("trends");
            List<TrendNews> result = new ArrayList<>();
            for (JsonNode node : trendNodes) {
                if (result.size() >= MAX_TRENDS) break;
                TrendNews trend = toTrendNews(node, articleById, minRelevance, periodStart, periodEnd);
                if (trend != null) result.add(trend);
            }
            log.info("Trend News {}건 파싱 완료", result.size());
            return result;
        } catch (Exception e) {
            log.error("Trend News 파싱 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private TrendNews toTrendNews(
            JsonNode node,
            Map<Long, Article> articleById,
            int minRelevance,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        Map<Long, TrendNewsArticle> sourceArticlesById = new LinkedHashMap<>();
        for (JsonNode sourceNode : node.path("sourceArticles")) {
            int relevance = sourceNode.path("relevance").asInt(0);
            Article article = articleById.get(sourceNode.path("id").asLong(0L));
            if (article == null || relevance < minRelevance) continue;
            TrendNewsArticle association = new TrendNewsArticle();
            association.setArticle(article);
            association.setRelevanceScore(relevance);
            sourceArticlesById.put(article.getId(), association);
        }
        List<TrendNewsArticle> sourceArticles = new ArrayList<>(sourceArticlesById.values());
        if (sourceArticles.size() < MIN_SOURCE_ARTICLES) {
            log.debug("근거 기사 부족으로 Trend News 제외: {}건", sourceArticles.size());
            return null;
        }

        String title = limit(node.path("title").asText().trim(), 200);
        String summary = limit(node.path("summary").asText().trim(), 500);
        String content = limit(node.path("content").asText().trim(), 5000);
        if (title.isBlank() || summary.isBlank() || content.isBlank()) return null;

        TrendNews trend = new TrendNews();
        trend.setPeriodStart(periodStart);
        trend.setPeriodEnd(periodEnd);
        trend.setTitle(title);
        trend.setSummary(summary);
        trend.setContent(content);
        trend.setTrendScore(clamp(node.path("hotScore").asInt(0)));
        trend.setConfidenceScore(clamp(node.path("confidence").asInt(0)));
        trend.setStatus("DRAFT");
        trend.setKeywords(readStrings(node.path("keywords"), 5, 100));
        trend.setActionItems(readStrings(node.path("actionItems"), 3, 500));
        trend.setSourceArticles(sourceArticles);
        // 같은 생성 실행의 Top 3가 점수 순으로 정렬되도록 동일한 생성 시각을 사용한다.
        trend.setGeneratedAt(periodEnd);
        sourceArticles.forEach(association -> association.setTrendNews(trend));
        return trend;
    }

    private List<Article> selectEvenlyDistributedSample(List<Article> articles) {
        if (articles.size() <= MAX_ARTICLES_FOR_TREND) return articles;
        Map<Long, Article> sampled = new LinkedHashMap<>();
        for (int index = 0; index < MAX_ARTICLES_FOR_TREND; index++) {
            int sourceIndex = (int) Math.round((double) index * (articles.size() - 1) / (MAX_ARTICLES_FOR_TREND - 1));
            Article article = articles.get(sourceIndex);
            sampled.put(article.getId(), article);
        }
        return new ArrayList<>(sampled.values());
    }

    private String buildArticlesJson(List<Article> articles) throws Exception {
        var articleNodes = objectMapper.createArrayNode();
        for (Article article : articles) {
            String text = article.getSummary() != null ? article.getSummary() : article.getOriginalContent();
            articleNodes.add(objectMapper.createObjectNode()
                    .put("id", article.getId())
                    .put("title", limit(article.getTitle(), 180))
                    .put("summary", limit(text, 220))
                    .put("competitor", article.getCompetitor())
                    .put("category", article.getCategory())
                    .put("source", article.getSourceName())
                    .put("publishedAt", article.getPublishedAt() != null ? article.getPublishedAt().toLocalDate().toString() : ""));
        }
        return objectMapper.writeValueAsString(articleNodes);
    }

    private List<String> readStrings(JsonNode node, int maximum, int maxLength) {
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = limit(item.asText().trim(), maxLength);
            if (!value.isBlank() && values.size() < maximum) values.add(value);
        }
        return values;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(value, 100));
    }

    private String limit(String value, int maximum) {
        if (value == null) return "";
        return value.length() <= maximum ? value : value.substring(0, maximum);
    }
}
