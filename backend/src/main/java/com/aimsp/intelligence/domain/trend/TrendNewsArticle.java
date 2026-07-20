package com.aimsp.intelligence.domain.trend;

import com.aimsp.intelligence.domain.article.Article;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trend_news_articles",
        uniqueConstraints = @UniqueConstraint(name = "uk_trend_news_article", columnNames = {"trend_news_id", "article_id"}),
        indexes = @Index(name = "idx_trend_news_articles_news", columnList = "trend_news_id"))
@Getter
@Setter
@NoArgsConstructor
public class TrendNewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trend_news_id", nullable = false)
    private TrendNews trendNews;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    private Integer relevanceScore;
}
