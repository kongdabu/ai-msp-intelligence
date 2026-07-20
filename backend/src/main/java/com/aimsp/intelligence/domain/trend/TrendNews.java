package com.aimsp.intelligence.domain.trend;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trend_news", indexes = {
    @Index(name = "idx_trend_news_generated_at", columnList = "generated_at"),
    @Index(name = "idx_trend_news_period", columnList = "period_start, period_end"),
    @Index(name = "idx_trend_news_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class TrendNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 500, nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Integer trendScore; // 0-100: 빈도·확산도·전략 관련도를 반영한 Hot Trend 점수
    private Integer confidenceScore; // 0-100: 근거 기사 충실도

    @Column(length = 20, nullable = false)
    private String status = "DRAFT"; // DRAFT|PUBLISHED

    @ElementCollection
    @CollectionTable(name = "trend_news_keywords", joinColumns = @JoinColumn(name = "trend_news_id"))
    @Column(name = "keyword", length = 100)
    private List<String> keywords = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "trend_news_action_items", joinColumns = @JoinColumn(name = "trend_news_id"))
    @Column(name = "action_item", length = 500)
    private List<String> actionItems = new ArrayList<>();

    @OneToMany(mappedBy = "trendNews", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrendNewsArticle> sourceArticles = new ArrayList<>();

    @Formula("(select count(*) from trend_news_articles tna where tna.trend_news_id = id)")
    private int sourceArticleCount;

    private LocalDateTime generatedAt;
}
