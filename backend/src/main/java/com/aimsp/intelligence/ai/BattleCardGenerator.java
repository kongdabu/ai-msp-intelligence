package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.battlecard.BattleCard;
import com.aimsp.intelligence.domain.battlecard.BattleCardArticle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleCardGenerator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> COMPETITOR_NAMES = Map.of(
            "LG_CNS", "LG CNS",
            "SK_AX", "SK AX",
            "BESPIN", "베스핀글로벌",
            "PWC", "PwC"
    );

    private static final String PROMPT_TEMPLATE = """
            너는 한국 AI MSP(Managed Service Provider) 영업 전략 전문가다.
            금융·공공 엔터프라이즈 시장에서 AI Agent 기반 ITO 전환 서비스를 제공하는 회사의 입장에서 분석해줘.

            [분석 대상 경쟁사]: %s

            아래 수집 기사를 분석하여 영업팀이 고객 미팅에서 즉시 활용 가능한 배틀카드를 생성해줘.

            [출력 지침]
            - strengths: 경쟁사의 기술/시장/레퍼런스 강점 (최대 3개, 각 50자 이내)
            - weaknesses: 기술/조직/가격 측면 약점 (최대 3개, 각 50자 이내)
            - opportunities: 우리 회사의 차별화 공략 포인트 (최대 3개, 각 50자 이내)
            - threats: 경쟁사가 우리 사업에 주는 위협 요인 (최대 3개, 각 50자 이내)
            - ourStrategy: 이 경쟁사와 맞붙을 때 핵심 대응 전략 (200자 이내)
            - impactScore: 이 경쟁사의 위협 강도 1(낮음)~5(높음)
            - sourceArticles: 분석에 실제 참조한 기사 id와 관련도(0-100), 최대 5개

            [반드시 JSON만 출력, 다른 텍스트 없이]
            {"competitor":"%s","strengths":["강점1","강점2"],"weaknesses":["약점1"],"opportunities":["기회1"],"threats":["위협1"],"ourStrategy":"전략 내용","impactScore":4,"sourceArticles":[{"id":1,"relevance":85}]}

            [수집 기사]
            %s
            """;

    public BattleCard generate(String competitor, List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            log.warn("[배틀카드] 기사 없음 — 스킵: {}", competitor);
            return null;
        }

        String competitorName = COMPETITOR_NAMES.getOrDefault(competitor, competitor);
        Map<Long, Article> articleById = articles.stream()
                .collect(java.util.stream.Collectors.toMap(Article::getId, a -> a));

        String articlesJson = buildArticlesJson(articles);
        String prompt = String.format(PROMPT_TEMPLATE, competitorName, competitor, articlesJson);

        try {
            String response = geminiApiClient.call(prompt);
            if (response == null) {
                log.warn("[배틀카드] Gemini 응답 없음: {}", competitor);
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            BattleCard bc = new BattleCard();
            bc.setCompetitor(root.path("competitor").asText(competitor));
            bc.setStrengths(toJsonString(root.path("strengths")));
            bc.setWeaknesses(toJsonString(root.path("weaknesses")));
            bc.setOpportunities(toJsonString(root.path("opportunities")));
            bc.setThreats(toJsonString(root.path("threats")));
            bc.setOurStrategy(root.path("ourStrategy").asText(""));
            bc.setImpactScore(root.path("impactScore").asInt(3));
            bc.setGeneratedAt(LocalDateTime.now());

            List<BattleCardArticle> bcArticles = new ArrayList<>();
            for (JsonNode sa : root.path("sourceArticles")) {
                int relevance = sa.path("relevance").asInt(0);
                if (relevance < 50) continue;
                Article a = articleById.get(sa.path("id").longValue());
                if (a != null) {
                    BattleCardArticle bca = new BattleCardArticle();
                    bca.setArticle(a);
                    bca.setRelevanceScore(relevance);
                    bcArticles.add(bca);
                }
            }
            if (bcArticles.isEmpty()) {
                for (Article a : articles) {
                    BattleCardArticle bca = new BattleCardArticle();
                    bca.setArticle(a);
                    bca.setRelevanceScore(100);
                    bcArticles.add(bca);
                }
            }
            bc.setSourceArticles(bcArticles);

            log.info("[배틀카드] 생성 완료: {} (출처 {}건)", competitor, bcArticles.size());
            return bc;
        } catch (Exception e) {
            log.error("[배틀카드] 파싱 실패 [{}]: {}", competitor, e.getMessage(), e);
            return null;
        }
    }

    private String buildArticlesJson(List<Article> articles) {
        try {
            List<Object> list = new ArrayList<>();
            for (Article a : articles) {
                String summary = a.getSummary() != null ? a.getSummary()
                        : (a.getOriginalContent() != null
                            ? a.getOriginalContent().substring(0, Math.min(150, a.getOriginalContent().length()))
                            : "");
                if (summary.length() > 150) summary = summary.substring(0, 150);
                final String s = summary;
                list.add(Map.of("id", a.getId(), "title", a.getTitle(), "summary", s));
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String toJsonString(JsonNode node) {
        try {
            if (node.isMissingNode() || node.isNull()) return "[]";
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "[]";
        }
    }
}
