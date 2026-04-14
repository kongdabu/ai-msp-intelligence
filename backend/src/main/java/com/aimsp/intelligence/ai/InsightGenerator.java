package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.insight.Insight;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightGenerator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    // 출력 인사이트 최대 4건, content 200자 이내로 제한 → 응답 토큰 절약
    private static final String INSIGHT_PROMPT_TEMPLATE = """
            너는 한국 AI MSP(Managed Service Provider) 사업의 수석 전략가다.
            금융·공공 엔터프라이즈 시장을 타겟으로 AI Agent 기반 ITO 전환 서비스를 제공한다.

            아래 최근 24시간 수집 뉴스를 분석하여 전략 인사이트를 도출해줘.

            [분석 프레임워크]
            1. 경쟁사 동향: LG CNS·SK AX·베스핀글로벌의 움직임과 우리 사업에 주는 시사점
            2. 기회 요인: AI Agent·Vertical AI·금융/공공 시장 기회 포착
            3. 위협 요인: 경쟁 심화·기술 변화·고객사 AI 내재화 리스크
            4. 전략적 권고: 즉시 검토 가능한 대응 방안

            [제약 조건]
            - 한국 금융규제(망분리, 전금법), 공공 보안 요건 고려
            - Agentic ITO 모델(헤드카운트 축소→성과 기반 전환) 관점 유지
            - 추상적 의견 금지, 구체적 사업 액션 위주
            - 인사이트는 반드시 4건 이하로 작성
            - content는 200자 이내로 작성
            - actionItems는 2개 이내로 작성

            [출력 형식 - 반드시 JSON만 출력, 다른 텍스트 없이]
            {"insights":[{"title":"제목30자이내","content":"분석200자이내","type":"OPPORTUNITY","competitor":"LG_CNS","impactScore":4,"actionItems":["액션1","액션2"]}]}

            type은 OPPORTUNITY|THREAT|TREND|STRATEGY 중 하나.
            competitor는 LG_CNS|SK_AX|BESPIN|PWC|GENERAL 중 하나.

            [수집 뉴스]
            %s
            """;

    /**
     * 기사 목록으로 전략 인사이트 생성
     */
    public List<Insight> generate(List<Article> articles) {
        List<Insight> result = new ArrayList<>();
        if (articles == null || articles.isEmpty()) return result;

        // 최대 15건만 전달해 입력 토큰 제한
        List<Article> limited = articles.size() > 15 ? articles.subList(0, 15) : articles;
        String articlesJson = buildArticlesJson(limited);
        String prompt = String.format(INSIGHT_PROMPT_TEMPLATE, articlesJson);

        try {
            String response = geminiApiClient.call(prompt);
            if (response == null) {
                log.warn("인사이트 생성 실패 (API 응답 없음)");
                return result;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode insightsNode = root.path("insights");

            for (JsonNode node : insightsNode) {
                Insight insight = new Insight();
                insight.setTitle(node.path("title").asText());
                insight.setContent(node.path("content").asText());
                insight.setInsightType(node.path("type").asText("TREND"));
                insight.setCompetitor(node.path("competitor").asText("GENERAL"));
                insight.setImpactScore(node.path("impactScore").asInt(3));
                insight.setGeneratedAt(LocalDateTime.now());

                List<String> actionItems = new ArrayList<>();
                for (JsonNode item : node.path("actionItems")) {
                    actionItems.add(item.asText());
                }
                insight.setActionItems(actionItems);
                // relevanceScore 상위 5개 기사만 근거로 저장
                List<Article> top5 = articles.stream()
                        .filter(a -> a.getRelevanceScore() != null)
                        .sorted(Comparator.comparingInt(Article::getRelevanceScore).reversed())
                        .limit(5)
                        .collect(java.util.stream.Collectors.toList());
                insight.setSourceArticles(top5);

                result.add(insight);
            }
            log.info("인사이트 {}건 파싱 완료", result.size());
        } catch (Exception e) {
            log.error("인사이트 파싱 실패: {}", e.getMessage(), e);
        }

        return result;
    }

    private String buildArticlesJson(List<Article> articles) {
        try {
            List<Object> articleList = new ArrayList<>();
            for (Article a : articles) {
                String summary = a.getSummary() != null ? a.getSummary()
                        : (a.getOriginalContent() != null
                            ? a.getOriginalContent().substring(0, Math.min(150, a.getOriginalContent().length()))
                            : "");
                // 요약은 150자로 제한
                if (summary.length() > 150) summary = summary.substring(0, 150);
                final String finalSummary = summary;
                articleList.add(new java.util.HashMap<>() {{
                    put("title", a.getTitle());
                    put("summary", finalSummary);
                    put("competitor", a.getCompetitor());
                    put("category", a.getCategory());
                }});
            }
            return objectMapper.writeValueAsString(articleList);
        } catch (Exception e) {
            log.error("기사 JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }

}
