package com.aimsp.intelligence.domain.insight;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "insight", indexes = {
    @Index(name = "idx_insight_generated_at", columnList = "generated_at"),
    @Index(name = "idx_insight_competitor", columnList = "competitor"),
    @Index(name = "idx_insight_impact_score", columnList = "impact_score")
})
@Getter
@Setter
@NoArgsConstructor
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String insightType; // OPPORTUNITY|THREAT|TREND|STRATEGY

    @Column(length = 50)
    private String competitor;

    private Integer impactScore; // 1-5

    @ElementCollection
    @CollectionTable(name = "insight_action_items", joinColumns = @JoinColumn(name = "insight_id"))
    @Column(name = "action_item", length = 500)
    private List<String> actionItems = new ArrayList<>();

    @OneToMany(mappedBy = "insight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsightArticle> sourceArticles = new ArrayList<>();

    private LocalDateTime generatedAt;
}
