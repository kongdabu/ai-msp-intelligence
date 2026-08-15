package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.exception.AiApiUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryGenerator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    private static final String SUMMARY_PROMPT_TEMPLATE = """
            너는 AI 생태계와 서비스 사업모델을 분석하는 전략 분석가다.
            다음 기사를 분석하여 한국어로 요약해줘.

            [요약 규칙]
            - 핵심 사실 위주, 3줄 이내
            - Frontier AI Labs, 파트너십, FDE·RDE·ODE, 컨설팅, Agentic AI·AIOps, AI 가격 정책·과금 모델, AI 인력 양성 관점에서 중요한 내용 우선
            - 200자 이내 (한국어 기준)
            - 관련도 점수: 0~100 (AI 생태계·서비스 사업모델과의 연관성)

            [출력 형식 - JSON only]
            {
              "summary": "요약 내용",
              "relevanceScore": 75,
              "detectedCompetitor": "GENERAL",
              "detectedCategory": "FRONTIER_LABS|AI_ECOSYSTEM|AI_DELIVERY_MODEL|CONSULTING|AGENTIC_OPERATIONS"
            }

            기사 제목: %s
            기사 내용: %s
            """;

    public record SummaryResult(
            String summary,
            int relevanceScore,
            String detectedCompetitor,
            String detectedCategory
    ) {}

    public SummaryResult generateSummary(String title, String content) {
        String truncatedContent = content != null && content.length() > 5000
                ? content.substring(0, 5000) + "..."
                : content;

        String prompt = String.format(SUMMARY_PROMPT_TEMPLATE, title, truncatedContent);

        try {
            String response = geminiApiClient.call(prompt);
            if (response == null) {
                log.warn("Gemini 요약 생성 실패 (API 응답 없음): {}", title);
                return null;
            }

            JsonNode node = objectMapper.readTree(response);

            return new SummaryResult(
                    node.path("summary").asText(null),
                    node.path("relevanceScore").asInt(0),
                    node.path("detectedCompetitor").asText("GENERAL"),
                    node.path("detectedCategory").asText("GEN_AI")
            );
        } catch (AiApiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 요약 파싱 실패: {} - {}", title, e.getMessage());
            return null;
        }
    }
}
