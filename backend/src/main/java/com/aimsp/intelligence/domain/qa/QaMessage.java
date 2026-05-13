package com.aimsp.intelligence.domain.qa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "qa_message", indexes = {
    @Index(name = "idx_qa_message_session_id", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
public class QaMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private QaSession session;

    @Column(length = 10, nullable = false)
    private String role; // USER | ASSISTANT

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String sourceArticleIds; // JSON 배열 "[1,2,3]" — ASSISTANT 메시지만

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
