package com.aimsp.intelligence.domain.insight;

import com.aimsp.intelligence.domain.article.Article;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insight_articles")
@Getter
@Setter
@NoArgsConstructor
public class InsightArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insight_id", nullable = false)
    private Insight insight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "relevance_score")
    private Integer relevanceScore; // 인사이트와의 관련도 (0-100)
}
