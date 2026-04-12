package com.aimsp.intelligence.domain.insight;

import com.aimsp.intelligence.domain.article.Article;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @ManyToMany
    @JoinTable(
            name = "insight_articles",
            joinColumns = @JoinColumn(name = "insight_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private List<Article> sourceArticles = new ArrayList<>();

    private LocalDateTime generatedAt;
}
