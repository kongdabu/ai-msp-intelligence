package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.radar.RadarPlayer;
import com.aimsp.intelligence.domain.radar.RadarSignal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyReportGenerator {

    private static final String REPORT_PROMPT_TEMPLATE = """
            너는 한국 금융·공공·엔터프라이즈 AI 시장을 전문으로 하는 AI 서비스 산업 전략 분석가다.
            아래 제공된 [검증된 산업 신호(Radar Signals)]를 종합 분석하여,
            "AI 서비스 산업 데일리 브리핑 및 국내 MSP 핵심 실행 과제"를 작성해줘.

            [분석 가이드라인]
            1. 개별 뉴스를 단순 나열하지 말고, 발견된 신호들을 유기적으로 연결하여 산업 구조의 구조적 변화를 설명한다.
            2. 다음 4대 핵심 축을 반드시 심층 분석한다:
               - Value Chain 재편: Frontier Labs의 SI/Consulting 영역 진입, Consulting사의 Build/Managed Service 확장, CSP의 Agent Platform 지배력, SI/MSP의 자체 에이전트 내재화
               - FDE/RDE/ODE 딜리버리: 현장 투입형 전문 인력 R&R, 고객 상주 방식, Commercial Model, 기존 M/M SI와의 구조적 차이
               - AI Pricing 전이: Seat → Consumption → Agent → Task → Outcome 단위 이동 및 FTE M/M에서 Platform Fee + Consumption + Outcome Incentive로의 전환
               - Agentic ITO / Ops: 기존 모니터링 중심 AIOps에서 자율 실행(Observe→Diagnose→Plan→Execute→Verify→Rollback) 루프로의 전환
            3. 최종적으로 '국내 MSP 관점 Top 3 Action'을 즉시 실행 가능한 수준으로 구체화하여 제시한다.
            4. 모든 텍스트는 전문적이고 실용적인 비즈니스 브리핑 톤(한국어)으로 작성한다.

            [검증된 산업 신호 목록 (최근 7~14일)]
            %s

            [출력 형식 - JSON only]
            {
              "title": "AI 서비스 산업 데일리 브리핑 (%s)",
              "executiveSummary": "경영진 핵심 요약 (3~5개 핵심 불릿 또는 구조화된 단락)",
              "valueChainImpact": "Consulting–SI–MSP–Application ITO 밸류체인 재편 및 상호 침투 분석",
              "fdeDeliveryAnalysis": "FDE/RDE/ODE 및 현장 투입형 딜리버리 모델 심층 분석",
              "pricingModelAnalysis": "AI 과금 체계 전이 및 SI/ITO 수익 모델 변화 분석",
              "agenticOpsAnalysis": "AIOps에서 Agentic ITO/자율 운영으로의 진화 분석",
              "mspOpportunitiesThreats": "국내 AI MSP 관점의 기회, 위협 및 구조적 리스크",
              "top3Actions": "1. [Action 1 제목]: 구체적 실행 방안\\n2. [Action 2 제목]: 구체적 실행 방안\\n3. [Action 3 제목]: 구체적 실행 방안"
            }
            """;

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    public record ReportResult(
            String title,
            String executiveSummary,
            String valueChainImpact,
            String fdeDeliveryAnalysis,
            String pricingModelAnalysis,
            String agenticOpsAnalysis,
            String mspOpportunitiesThreats,
            String top3Actions
    ) {
    }

    public ReportResult generateReport(List<RadarSignal> signals, LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }

        String periodLabel = periodStart.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " ~ "
                + periodEnd.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        StringBuilder signalsContext = new StringBuilder();
        for (int i = 0; i < signals.size(); i++) {
            RadarSignal s = signals.get(i);
            signalsContext.append(String.format("### 신호 %d: %s\n", i + 1, s.getTitle()));
            signalsContext.append(String.format("- 사실 요약: %s\n", s.getFact()));
            signalsContext.append(String.format("- 관련 기업: %s\n", s.getPlayers().stream().map(RadarPlayer::getName).sorted().collect(Collectors.joining(", "))));
            signalsContext.append(String.format("- 관점(Lens): %s (영향도: %d, 신뢰도: %d)\n", String.join(", ", s.getLenses()), s.getImpactScore(), s.getConfidenceScore()));
            if (s.getAssessment() != null) {
                signalsContext.append(String.format("- 변화점: %s\n", s.getAssessment().getWhatChanged()));
                signalsContext.append(String.format("- 구조 영향: %s\n", s.getAssessment().getIndustryStructureImpact()));
                signalsContext.append(String.format("- 권고 행동: %s\n", s.getAssessment().getRecommendedAction()));
            }
            signalsContext.append("\n");
        }

        String prompt = String.format(REPORT_PROMPT_TEMPLATE, signalsContext, periodLabel);

        try {
            String response = geminiApiClient.call(prompt);
            if (response == null || response.isBlank()) {
                log.warn("Gemini 전략 보고서 생성 실패 (응답 없음)");
                return null;
            }

            JsonNode node = objectMapper.readTree(response);
            return new ReportResult(
                    node.path("title").asText("AI 서비스 산업 데일리 브리핑"),
                    node.path("executiveSummary").asText(""),
                    node.path("valueChainImpact").asText(""),
                    node.path("fdeDeliveryAnalysis").asText(""),
                    node.path("pricingModelAnalysis").asText(""),
                    node.path("agenticOpsAnalysis").asText(""),
                    node.path("mspOpportunitiesThreats").asText(""),
                    node.path("top3Actions").asText("")
            );
        } catch (Exception e) {
            log.error("전략 보고서 파싱 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}
