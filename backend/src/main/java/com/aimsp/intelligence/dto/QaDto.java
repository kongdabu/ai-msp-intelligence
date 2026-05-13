package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.qa.QaMessage;
import com.aimsp.intelligence.domain.qa.QaSession;

import java.time.LocalDateTime;
import java.util.List;

public class QaDto {

    public record SessionResponse(
            Long id,
            String title,
            LocalDateTime createdAt,
            int messageCount
    ) {
        public static SessionResponse from(QaSession session) {
            return new SessionResponse(
                    session.getId(),
                    session.getTitle(),
                    session.getCreatedAt(),
                    session.getMessages().size()
            );
        }
    }

    public record AskRequest(String question) {}

    public record ArticleRef(
            Long id,
            String title,
            String url,
            String competitor,
            String category
    ) {
        public static ArticleRef from(Article a) {
            return new ArticleRef(a.getId(), a.getTitle(), a.getUrl(),
                    a.getCompetitor(), a.getCategory());
        }
    }

    public record MessageResponse(
            Long id,
            String role,
            String content,
            List<ArticleRef> sourceArticles,
            LocalDateTime createdAt
    ) {
        public static MessageResponse from(QaMessage msg, List<Article> sources) {
            List<ArticleRef> refs = sources.stream().map(ArticleRef::from).toList();
            return new MessageResponse(
                    msg.getId(),
                    msg.getRole(),
                    msg.getContent(),
                    refs,
                    msg.getCreatedAt()
            );
        }
    }
}
