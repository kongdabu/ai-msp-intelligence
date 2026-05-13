package com.aimsp.intelligence.domain.qa;

import com.aimsp.intelligence.ai.QaAnswerGenerator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.QaDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaService {

    private final QaSessionRepository sessionRepository;
    private final QaMessageRepository messageRepository;
    private final ArticleRepository articleRepository;
    private final QaAnswerGenerator qaAnswerGenerator;
    private final ObjectMapper objectMapper;

    @Transactional
    public QaDto.SessionResponse createSession() {
        QaSession session = new QaSession();
        session.setTitle("새 대화");
        QaSession saved = sessionRepository.save(session);
        return new QaDto.SessionResponse(saved.getId(), saved.getTitle(), saved.getCreatedAt(), 0);
    }

    @Transactional(readOnly = true)
    public List<QaDto.SessionResponse> getSessions() {
        return sessionRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(s -> new QaDto.SessionResponse(
                        s.getId(), s.getTitle(), s.getCreatedAt(), s.getMessages().size()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QaDto.MessageResponse> getMessages(Long sessionId) {
        List<QaMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream()
                .map(msg -> QaDto.MessageResponse.from(msg, resolveSourceArticles(msg)))
                .toList();
    }

    @Transactional
    public QaDto.MessageResponse ask(Long sessionId, String question) {
        QaSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // 첫 질문으로 세션 제목 설정
        if ("새 대화".equals(session.getTitle())) {
            session.setTitle(question.length() > 50 ? question.substring(0, 50) + "…" : question);
        }

        // USER 메시지 저장
        QaMessage userMsg = new QaMessage();
        userMsg.setSession(session);
        userMsg.setRole("USER");
        userMsg.setContent(question);
        messageRepository.save(userMsg);

        // 이전 3턴(6메시지) 히스토리 로드 (최신순 → 역순 정렬)
        List<QaMessage> recentHistory = new ArrayList<>(
                messageRepository.findTop6BySessionIdOrderByCreatedAtDesc(sessionId)
        );
        Collections.reverse(recentHistory);

        // AI 답변 생성
        log.info("[Q&A] 질문 처리 [session={}]: {}", sessionId, question);
        QaAnswerGenerator.QaResult result = qaAnswerGenerator.answer(question, recentHistory);

        // ASSISTANT 메시지 저장
        QaMessage assistantMsg = new QaMessage();
        assistantMsg.setSession(session);
        assistantMsg.setRole("ASSISTANT");
        assistantMsg.setContent(result.answer());
        assistantMsg.setSourceArticleIds(toJson(result.sourceArticleIds()));
        messageRepository.save(assistantMsg);

        // 소스 기사 조회
        List<Article> sourceArticles = result.sourceArticleIds().isEmpty()
                ? List.of()
                : articleRepository.findAllById(result.sourceArticleIds());

        log.info("[Q&A] 답변 완료 [session={}, 출처={}건]", sessionId, sourceArticles.size());
        return QaDto.MessageResponse.from(assistantMsg, sourceArticles);
    }

    private List<Article> resolveSourceArticles(QaMessage msg) {
        if (!"ASSISTANT".equals(msg.getRole()) || msg.getSourceArticleIds() == null) {
            return List.of();
        }
        try {
            List<Long> ids = objectMapper.readValue(
                    msg.getSourceArticleIds(), new TypeReference<List<Long>>() {}
            );
            return ids.isEmpty() ? List.of() : articleRepository.findAllById(ids);
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            return "[]";
        }
    }
}
