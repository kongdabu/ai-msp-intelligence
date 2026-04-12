package com.aimsp.intelligence.domain.article;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 2000)
    private String url;

    @Column(length = 500)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String originalContent; // 원문 최대 5,000자

    @Column(length = 500)
    private String summary; // AI 요약 200자

    @Column(length = 50)
    private String competitor; // LG_CNS|SK_AX|BESPIN|PWC|GENERAL

    @Column(length = 50)
    private String category; // AI_AGENT|VERTICAL_AI|ITO|MSP|CLOUD|GEN_AI

    @Column(length = 50)
    private String sourceType; // NEWS|HOMEPAGE|SNS|IDC_REPORT

    @Column(length = 200)
    private String sourceName;

    private LocalDateTime publishedAt;
    private LocalDateTime collectedAt;

    private Boolean isProcessed = false;
    private Integer relevanceScore; // 0-100
}
