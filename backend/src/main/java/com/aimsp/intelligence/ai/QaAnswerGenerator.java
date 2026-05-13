package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.domain.insight.Insight;
import com.aimsp.intelligence.domain.insight.InsightRepository;
import com.aimsp.intelligence.domain.qa.QaMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QaAnswerGenerator {

    private static final int MAX_ARTICLES = 15;
    private static final int MAX_INSIGHTS = 5;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final GeminiApiClient geminiApiClient;
    private final EmbeddingApiClient embeddingApiClient;
    private final ArticleRepository articleRepository;
    private final InsightRepository insightRepository;
    private final ObjectMapper objectMapper;

    public record QaResult(String answer, List<Long> sourceArticleIds) {}

    public QaResult answer(String question, List<QaMessage> recentHistory) {
        // 1. 질문 임베딩 생성 → 벡터 유사도 검색 (실패 시 LIKE fallback)
        List<Article> articles = retrieveContext(question);

        // 2. 최근 90일 인사이트 top5
        List<Insight> insights = insightRepository
                .findByGeneratedAtAfterOrderByImpactScoreDesc(LocalDateTime.now().minusDays(90))
                .stream().limit(MAX_INSIGHTS).toList();

        // 3. Gemini 프롬프트 조립 및 호출
        String prompt = buildPrompt(question, articles, insights, recentHistory);
        String response = geminiApiClient.call(prompt);
        if (response == null) {
            return new QaResult("AI API가 응답하지 않습니다. 잠시 후 다시 시도해주세요.", List.of());
        }

        // 4. 응답 파싱
        return parseResult(response);
    }

    private List<Article> retrieveContext(String question) {
        try {
            float[] queryEmbedding = embeddingApiClient.embed(
                    question, EmbeddingApiClient.TaskType.RETRIEVAL_QUERY
            );
            if (queryEmbedding != null) {
                String embeddingStr = Arrays.toString(queryEmbedding);
                List<Article> results = articleRepository.findSimilarArticles(embeddingStr, MAX_ARTICLES);
                if (!results.isEmpty()) {
                    log.debug("[Q&A] 벡터 검색 {}건", results.size());
                    return results;
                }
            }
        } catch (Exception e) {
            log.debug("[Q&A] 벡터 검색 불가, LIKE fallback: {}", e.getMessage());
        }

        // LIKE fallback (H2 환경 또는 임베딩 없는 경우)
        String keyword = question.length() > 50 ? question.substring(0, 50) : question;
        List<Article> fallback = articleRepository.findByKeywordForContext(
                keyword, PageRequest.of(0, MAX_ARTICLES)
        );
        log.debug("[Q&A] LIKE 검색 {}건", fallback.size());
        return fallback;
    }

    private String buildPrompt(String question, List<Article> articles,
                                List<Insight> insights, List<QaMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                너는 AI MSP 전략 전문가다. 아래 컨텍스트를 바탕으로 질문에 한국어로 답변해줘.
                규칙:
                - 컨텍스트에 없는 내용은 "관련 데이터 없음"으로 명시
                - 답변 400자 이내
                - sourceArticleIds에 답변 근거 기사 ID 반드시 포함

                """);

        // 관련 기사
        sb.append("[관련 기사]\n");
        if (articles.isEmpty()) {
            sb.append("(없음)\n");
        } else {
            for (int i = 0; i < articles.size(); i++) {
                Article a = articles.get(i);
                sb.append(String.format("(%d) [%s | %s | %s] \"%s\" — %s\n",
                        i + 1,
                        a.getCompetitor() != null ? a.getCompetitor() : "GENERAL",
                        a.getCategory() != null ? a.getCategory() : "-",
                        a.getPublishedAt() != null ? a.getPublishedAt().format(DATE_FMT) : "-",
                        a.getTitle() != null ? a.getTitle() : "",
                        a.getSummary() != null ? a.getSummary() : ""
                ));
            }
        }

        // 최근 인사이트
        sb.append("\n[최근 인사이트]\n");
        if (insights.isEmpty()) {
            sb.append("(없음)\n");
        } else {
            for (int i = 0; i < insights.size(); i++) {
                Insight ins = insights.get(i);
                sb.append(String.format("(%d) [%s | 점수:%d] \"%s\" — %s\n",
                        i + 1,
                        ins.getInsightType() != null ? ins.getInsightType() : "-",
                        ins.getImpactScore() != null ? ins.getImpactScore() : 0,
                        ins.getTitle() != null ? ins.getTitle() : "",
                        ins.getContent() != null ? ins.getContent() : ""
                ));
            }
        }

        // 대화 기록 (최근 3턴)
        if (!history.isEmpty()) {
            sb.append("\n[대화 기록]\n");
            List<QaMessage> ordered = new ArrayList<>(history);
            Collections.reverse(ordered);
            for (QaMessage msg : ordered) {
                String roleLabel = "USER".equals(msg.getRole()) ? "User" : "Assistant";
                sb.append(roleLabel).append(": ").append(msg.getContent()).append("\n");
            }
        }

        sb.append("\n[현재 질문]\n").append(question);
        sb.append("\n\n[출력 JSON만]\n");
        sb.append("{\"answer\": \"답변내용\", \"sourceArticleIds\": [1, 2]}");

        return sb.toString();
    }

    private QaResult parseResult(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String answer = root.path("answer").asText("답변을 생성할 수 없습니다.");
            List<Long> ids = new ArrayList<>();
            for (JsonNode id : root.path("sourceArticleIds")) {
                ids.add(id.asLong());
            }
            return new QaResult(answer, ids);
        } catch (Exception e) {
            log.warn("[Q&A] 응답 파싱 실패, 원문 반환: {}", e.getMessage());
            return new QaResult(response, List.of());
        }
    }
}
