package com.aimsp.intelligence.ai;

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

    // 무료 티어 15 RPM 제한 준수: 기사당 4초 간격 (60s / 15 = 4s)
    private static final long API_CALL_DELAY_MS = 4000;

    private static final String SUMMARY_PROMPT_TEMPLATE = """
            너는 AI MSP(Managed Service Provider) 사업 전략 분석가다.
            다음 IT 서비스 업계 기사를 분석하여 한국어로 요약해줘.

            [요약 규칙]
            - 핵심 사실 위주, 3줄 이내
            - AI·AI Agent·ITO·MSP 관점에서 중요한 내용 우선
            - 경쟁사(LG CNS, SK AX, 베스핀글로벌, PwC)가 언급되면 반드시 포함
            - 200자 이내 (한국어 기준)
            - 관련도 점수: 0~100 (AI MSP 사업과의 연관성)

            [출력 형식 - JSON only]
            {
              "summary": "요약 내용",
              "relevanceScore": 75,
              "detectedCompetitor": "LG_CNS|SK_AX|BESPIN|PWC|GENERAL",
              "detectedCategory": "AI_AGENT|VERTICAL_AI|ITO|MSP|CLOUD|GEN_AI"
            }

            기사 제목: %s
            기사 내용: %s
            """;

    /**
     * 기사 요약 생성 결과
     */
    public record SummaryResult(
            String summary,
            int relevanceScore,
            String detectedCompetitor,
            String detectedCategory
    ) {}

    /**
     * 기사 제목과 내용으로 AI 요약 생성
     */
    public SummaryResult generateSummary(String title, String content) {
        // 내용을 5000자로 제한
        String truncatedContent = content != null && content.length() > 5000
                ? content.substring(0, 5000) + "..."
                : content;

        String prompt = String.format(SUMMARY_PROMPT_TEMPLATE, title, truncatedContent);

        try {
            Thread.sleep(API_CALL_DELAY_MS);
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.error("AI 요약 파싱 실패: {} - {}", title, e.getMessage());
            return null;
        }
    }

}
