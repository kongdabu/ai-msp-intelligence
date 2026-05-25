package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.config.SystemConfig;
import com.aimsp.intelligence.domain.config.SystemConfigService;
import com.aimsp.intelligence.domain.insight.Insight;
import com.aimsp.intelligence.domain.insight.InsightArticle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightGenerator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;
    private final SystemConfigService systemConfigService;

    private static final String INSIGHT_PROMPT_TEMPLATE = """
            너는 한국 AI MSP(Managed Service Provider) 사업의 수석 전략가다.
            금융·공공 엔터프라이즈 시장을 타겟으로 AI Agent 기반 ITO 전환 서비스를 제공한다.

            아래 최근 수집 뉴스를 분석하여 전략 인사이트를 도출해줘.

            [분석 프레임워크]
            1. 경쟁사 동향: LG CNS·SK AX·베스핀글로벌의 움직임과 우리 사업에 주는 시사점
            2. 기회 요인: AI Agent·Vertical AI·금융/공공 시장 기회 포착
            3. 위협 요인: 경쟁 심화·기술 변화·고객사 AI 내재화 리스크
            4. 전략적 권고: 즉시 검토 가능한 대응 방안

            [제약 조건]
            - 한국 금융규제(망분리, 전금법), 공공 보안 요건 고려
            - Agentic ITO 모델(헤드카운트 축소→성과 기반 전환) 관점 유지
            - 추상적 의견 금지, 구체적 사업 액션 위주
            - 인사이트는 반드시 %d건 이하로 작성
            - content는 200자 이내로 작성
            - actionItems는 2개 이내로 작성
            - OPPORTUNITY·THREAT·TREND·STRATEGY 각 타입 1건 이상 포함할 것
            - 한 경쟁사에만 집중하지 말 것

            [좋은 인사이트 예시 - 이런 수준으로 작성]
            title: "LG CNS, 금융 AI Agent 3종 출시로 ITO 영역 직접 잠식"
            content: "LG CNS가 KB·신한 대상 AI Agent 패키지를 발표하고 기존 ITO 인력 30%% 절감 효과를 강조. 우리의 헤드카운트 절감형 ITO 모델과 직접 충돌하는 포지셔닝."
            actionItems: ["KB·신한 담당 영업팀에 차별화 포인트 즉시 공유", "당사 AI Agent ROI 수치를 LG CNS 발표 수치 대비 비교 자료 준비"]

            [나쁜 인사이트 예시 - 절대 생성 금지]
            title: "AI 시장이 빠르게 성장 중"
            content: "AI 기술이 다양한 산업에서 활용되고 있으며 앞으로도 성장이 예상됨"
            → 근거 없는 일반론, 비즈니스 액션 불명확

            [중복 방지 - 아래 최근 인사이트와 실질적으로 동일한 내용은 생성 금지]
            %s

            [출력 형식 - 반드시 JSON만 출력, 다른 텍스트 없이]
            {"insights":[{"title":"제목30자이내","content":"분석200자이내","type":"OPPORTUNITY","competitor":"LG_CNS","impactScore":4,"confidence":82,"actionItems":["액션1","액션2"],"sourceArticles":[{"id":1,"relevance":85},{"id":3,"relevance":72}]}]}

            type은 OPPORTUNITY|THREAT|TREND|STRATEGY 중 하나.
            competitor는 LG_CNS|SK_AX|BESPIN|PWC|GENERAL 중 하나.
            confidence는 0-100 (이 인사이트의 근거 충실도 자체 평가).
            sourceArticles는 인사이트 도출에 실제로 참조한 기사 목록(최대 5개, 최소 3개). id는 기사 id, relevance는 이 인사이트와의 관련도(0-100 정수).

            [수집 뉴스]
            %s
            """; // %d: maxInsights, %s: recentInsightsJson, %s: articlesJson

    /**
     * 기사 목록으로 전략 인사이트 생성.
     * recentInsights: 최근 7일 기존 인사이트 (중복 방지용 컨텍스트)
     */
    public List<Insight> generate(List<Article> articles, List<Insight> recentInsights) {
        List<Insight> result = new ArrayList<>();
        if (articles == null || articles.isEmpty()) return result;

        SystemConfig config = systemConfigService.getOrCreate();
        int maxInsights = config.getMaxInsightsPerGeneration();
        int maxArticles = config.getMaxArticlesForInsight();
        int minRelevance = config.getMinRelevanceScoreForInsight();

        List<Article> limited = articles.size() > maxArticles ? articles.subList(0, maxArticles) : articles;
        // id → Article 맵 (Gemini가 반환한 sourceArticleIds로 역조회)
        java.util.Map<Long, Article> articleById = limited.stream()
                .collect(java.util.stream.Collectors.toMap(Article::getId, a -> a));

        String recentInsightsJson = buildRecentInsightsJson(recentInsights);
        String articlesJson = buildArticlesJson(limited);
        String prompt = String.format(INSIGHT_PROMPT_TEMPLATE, maxInsights, recentInsightsJson, articlesJson);

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

                // Gemini 자체 평가 confidence (Validator가 후에 덮어씀)
                if (!node.path("confidence").isMissingNode()) {
                    insight.setConfidenceScore(node.path("confidence").asInt());
                }

                List<String> actionItems = new ArrayList<>();
                for (JsonNode item : node.path("actionItems")) {
                    actionItems.add(item.asText());
                }
                insight.setActionItems(actionItems);

                // Gemini가 반환한 sourceArticles에서 minRelevance 이상인 기사만 연결
                List<InsightArticle> insightArticles = new ArrayList<>();
                JsonNode sourceArticlesNode = node.path("sourceArticles");
                if (!sourceArticlesNode.isMissingNode() && sourceArticlesNode.isArray()) {
                    for (JsonNode saNode : sourceArticlesNode) {
                        int relevance = saNode.path("relevance").asInt(0);
                        if (relevance < minRelevance) continue;
                        Article a = articleById.get(saNode.path("id").asLong(0L));
                        if (a != null) {
                            InsightArticle ia = new InsightArticle();
                            ia.setArticle(a);
                            ia.setRelevanceScore(relevance);
                            insightArticles.add(ia);
                        }
                    }
                }
                // Gemini가 sourceArticles를 반환하지 않은 경우 → limited 전체를 폴백으로 사용
                if (insightArticles.isEmpty()) {
                    for (Article a : limited) {
                        InsightArticle ia = new InsightArticle();
                        ia.setArticle(a);
                        ia.setRelevanceScore(100);
                        insightArticles.add(ia);
                    }
                }
                insight.setSourceArticles(insightArticles);

                result.add(insight);
            }
            log.info("인사이트 {}건 파싱 완료", result.size());
        } catch (Exception e) {
            log.error("인사이트 파싱 실패: {}", e.getMessage(), e);
        }

        return result;
    }

    private String buildRecentInsightsJson(List<Insight> recentInsights) {
        if (recentInsights == null || recentInsights.isEmpty()) return "없음 (첫 생성)";
        try {
            var arr = objectMapper.createArrayNode();
            for (Insight ins : recentInsights) {
                arr.add(objectMapper.createObjectNode()
                        .put("title", ins.getTitle())
                        .put("content", ins.getContent())
                        .put("type", ins.getInsightType())
                        .put("competitor", ins.getCompetitor()));
            }
            return objectMapper.writeValueAsString(arr);
        } catch (Exception e) {
            log.error("최근 인사이트 JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }

    private String buildArticlesJson(List<Article> articles) {
        try {
            var arr = objectMapper.createArrayNode();
            for (Article a : articles) {
                String summary = a.getSummary() != null ? a.getSummary()
                        : (a.getOriginalContent() != null
                            ? a.getOriginalContent().substring(0, Math.min(150, a.getOriginalContent().length()))
                            : "");
                if (summary.length() > 150) summary = summary.substring(0, 150);
                arr.add(objectMapper.createObjectNode()
                        .put("id", a.getId())
                        .put("title", a.getTitle())
                        .put("summary", summary)
                        .put("competitor", a.getCompetitor())
                        .put("category", a.getCategory()));
            }
            return objectMapper.writeValueAsString(arr);
        } catch (Exception e) {
            log.error("기사 JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }
}
