package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.domain.article.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RadarSignalAnalyzer {

    private static final String PROMPT_TEMPLATE = """
            너는 AI 서비스 산업 전략 분석가다. 아래 원문에서 검증 가능한 사실만 사용해 AI Services Industry Radar 신호를 판별한다.
            추측, 원문에 없는 수치·계약·사업자명은 절대 만들지 않는다. 중요하지 않으면 isRelevant=false로 응답한다.

            [Radar Lens]
            AI_AGENT: AI Agent 제품·플랫폼과 자율 업무 실행 구조
            FRONTIER_LABS: 모델 경쟁력·생태계 지배력
            PARTNERSHIP: 모델사·클라우드·SI의 결합 및 판매 구조
            DEPLOYMENT_MODEL: FDE·RDE·ODE 등 현장 투입형 AI 딜리버리
            AI_PRICING: 사용량·성과형 등 AI 과금 모델
            AGENTIC_OPERATIONS: AIOps·운영 자동화·Agentic ITO

            [Watch List]
            %s

            [출력 규칙]
            - JSON만 응답한다.
            - playerNames는 Watch List에 있는 이름만 쓴다.
            - lenses는 위 코드 중 1~3개만 쓴다.
            - fact는 원문에 근거한 한국어 300자 이내 사실 요약이다.
            - 점수는 0~100 정수다.
            - 사업 구조 영향과 권고 행동은 한국 AI MSP 관점에서, 사실과 해석을 구분해 간결하게 쓴다.
            {
              "isRelevant": true,
              "fact": "",
              "lenses": ["PARTNERSHIP"],
              "playerNames": ["OpenAI"],
              "confidenceScore": 80,
              "impactScore": 75,
              "signalType": "PARTNERSHIP|PRODUCT|DELIVERY_MODEL|PRICING|OPERATIONS|TALENT",
              "whatChanged": "",
              "industryStructureImpact": "",
              "mspOpportunity": "",
              "mspThreat": "",
              "structuralRisk": "",
              "recommendedAction": ""
            }

            기사 제목: %s
            기사 원문: %s
            """;

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    public AnalysisResult analyze(Article article, List<RadarPlayer> watchlist) {
        String content = article.getOriginalContent();
        if (content == null || content.isBlank()) return null;
        String playerNames = watchlist.stream().map(RadarPlayer::getName).sorted().reduce((left, right) -> left + ", " + right).orElse("");
        String truncatedContent = content.length() > 6000 ? content.substring(0, 6000) : content;
        try {
            String response = geminiApiClient.call(String.format(PROMPT_TEMPLATE, playerNames, article.getTitle(), truncatedContent));
            if (response == null) return null;
            JsonNode node = objectMapper.readTree(response);
            if (!node.path("isRelevant").asBoolean(false)) return null;

            Set<String> lenses = objectMapper.convertValue(node.path("lenses"), objectMapper.getTypeFactory()
                    .constructCollectionType(Set.class, String.class));
            Set<String> players = objectMapper.convertValue(node.path("playerNames"), objectMapper.getTypeFactory()
                    .constructCollectionType(Set.class, String.class));
            if (lenses == null || lenses.isEmpty() || players == null || players.isEmpty()) return null;

            return new AnalysisResult(
                    node.path("fact").asText(), lenses, players,
                    clamp(node.path("confidenceScore").asInt()), clamp(node.path("impactScore").asInt()),
                    node.path("signalType").asText("MARKET_MOVE"), node.path("whatChanged").asText(),
                    node.path("industryStructureImpact").asText(), nullableText(node, "mspOpportunity"),
                    nullableText(node, "mspThreat"), nullableText(node, "structuralRisk"),
                    node.path("recommendedAction").asText()
            );
        } catch (Exception e) {
            log.warn("Radar 신호 분석 실패: {}", article.getTitle());
            return null;
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String nullableText(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    public record AnalysisResult(String fact, Set<String> lenses, Set<String> playerNames, int confidenceScore,
                                 int impactScore, String signalType, String whatChanged,
                                 String industryStructureImpact, String mspOpportunity, String mspThreat,
                                 String structuralRisk, String recommendedAction) {
    }
}
