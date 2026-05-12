package com.aimsp.intelligence.domain.battlecard;

import com.aimsp.intelligence.domain.article.Article;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "battle_card_articles")
@Getter
@Setter
@NoArgsConstructor
public class BattleCardArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_card_id", nullable = false)
    private BattleCard battleCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "relevance_score")
    private Integer relevanceScore;
}
