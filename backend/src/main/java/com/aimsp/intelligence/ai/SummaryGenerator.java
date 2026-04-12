package com.aimsp.intelligence.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryGenerator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    // 15 RPM 제한: 최소 호출 간격 4초
    private static final long MIN_INTERVAL_MS = 4000;
    private final AtomicLong lastCallTime = new AtomicLong(0);

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
     * 고정 sleep 대신 스마트 Rate Limiter: 마지막 호출 이후 경과 시간만큼만 대기
     */
    public SummaryResult generateSummary(String title, String content) {
        // 내용을 5000자로 제한
        String truncatedContent = content != null && content.length() > 5000
                ? content.substring(0, 5000) + "..."
                : content;

        String prompt = String.format(SUMMARY_PROMPT_TEMPLATE, title, truncatedContent);

        try {
            applyRateLimit();
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

    /**
     * 스마트 Rate Limiter: 마지막 호출 후 경과 시간이 MIN_INTERVAL_MS 미만일 때만 대기
     * Gemini 응답이 4초 이상 걸리면 추가 대기 없이 즉시 다음 호출 가능
     */
    private synchronized void applyRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastCallTime.get();
        if (lastCallTime.get() > 0 && elapsed < MIN_INTERVAL_MS) {
            long waitMs = MIN_INTERVAL_MS - elapsed;
            log.debug("Rate limit 대기: {}ms", waitMs);
            Thread.sleep(waitMs);
        }
        lastCallTime.set(System.currentTimeMillis());
    }
}
