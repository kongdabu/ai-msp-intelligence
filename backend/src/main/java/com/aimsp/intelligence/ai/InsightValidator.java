package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.insight.Insight;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightValidator {

    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    private static final int CONFIDENCE_THRESHOLD = 60;

    private static final String VALIDATION_PROMPT_TEMPLATE = """
            너는 AI MSP 전략 인사이트의 품질 검토관이다.
            아래 생성된 인사이트들의 품질을 평가해줘.

            [평가 기준]
            - confidence (0-100): 구체적 사실 기반이고 액션이 명확한가?
              * 70 이상: 구체적 근거, 명확한 비즈니스 액션
              * 60-69: 근거 있으나 일반적
              * 60 미만: 추상적이거나 일반론, 근거 불충분
            - isDuplicate: 아래 기존 인사이트와 실질적으로 동일한 내용인가?

            [최근 7일 기존 인사이트]
            %s

            [검토할 인사이트]
            %s

            [출력 형식 - JSON only]
            {"validations":[{"index":0,"confidence":85,"reasoning":"판단근거","isDuplicate":false}]}
            """;

    /**
     * 생성된 인사이트를 검증하여 품질·중복 기준을 통과한 것만 반환.
     * API 실패 시 전체 통과 처리(안전 폴백).
     */
    public List<Insight> validate(List<Insight> candidates, List<Insight> recentInsights) {
        if (candidates.isEmpty()) return candidates;

        String recentJson = buildJson(recentInsights.stream().map(ins -> {
            var node = objectMapper.createObjectNode();
            node.put("title", ins.getTitle());
            node.put("content", ins.getContent());
            node.put("type", ins.getInsightType());
            node.put("competitor", ins.getCompetitor());
            return (Object) node;
        }).toList());

        String candidatesJson = buildCandidatesJson(candidates);
        String prompt = String.format(VALIDATION_PROMPT_TEMPLATE, recentJson, candidatesJson);

        try {
            String response = geminiApiClient.call(prompt);
            if (response == null) {
                log.warn("인사이트 검증 API 응답 없음 - 전체 통과 처리");
                return candidates;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode validations = root.path("validations");

            List<Insight> passed = new ArrayList<>();
            int duplicateCount = 0;
            int lowQualityCount = 0;

            for (JsonNode v : validations) {
                int index = v.path("index").asInt(-1);
                if (index < 0 || index >= candidates.size()) continue;

                int confidence = v.path("confidence").asInt(0);
                boolean isDuplicate = v.path("isDuplicate").asBoolean(false);
                String reasoning = v.path("reasoning").asText("");

                Insight insight = candidates.get(index);

                if (isDuplicate) {
                    duplicateCount++;
                    log.debug("중복 인사이트 제외: {}", insight.getTitle());
                    continue;
                }
                if (confidence < CONFIDENCE_THRESHOLD) {
                    lowQualityCount++;
                    log.debug("저품질 인사이트 제외 (confidence={}): {}", confidence, insight.getTitle());
                    continue;
                }

                insight.setConfidenceScore(confidence);
                insight.setValidationReason(reasoning);
                passed.add(insight);
            }

            log.info("인사이트 검증 완료 - 통과: {}건 / 중복 제외: {}건 / 저품질 제외: {}건",
                    passed.size(), duplicateCount, lowQualityCount);
            return passed;

        } catch (Exception e) {
            log.error("인사이트 검증 실패: {} - 전체 통과 처리", e.getMessage());
            return candidates;
        }
    }

    private String buildCandidatesJson(List<Insight> candidates) {
        var list = new ArrayList<Object>();
        for (int i = 0; i < candidates.size(); i++) {
            Insight ins = candidates.get(i);
            var node = objectMapper.createObjectNode();
            node.put("index", i);
            node.put("title", ins.getTitle());
            node.put("content", ins.getContent());
            node.put("type", ins.getInsightType());
            node.put("competitor", ins.getCompetitor());
            var actions = objectMapper.createArrayNode();
            if (ins.getActionItems() != null) ins.getActionItems().forEach(actions::add);
            node.set("actionItems", actions);
            list.add(node);
        }
        return buildJson(list);
    }

    private String buildJson(List<Object> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }
}
