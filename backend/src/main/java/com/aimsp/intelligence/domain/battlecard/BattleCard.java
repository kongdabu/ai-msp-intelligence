package com.aimsp.intelligence.domain.battlecard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "battle_card", indexes = {
    @Index(name = "idx_battlecard_competitor", columnList = "competitor"),
    @Index(name = "idx_battlecard_generated_at", columnList = "generated_at")
})
@Getter
@Setter
@NoArgsConstructor
public class BattleCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String competitor; // LG_CNS | SK_AX | BESPIN | PWC

    @Column(columnDefinition = "TEXT")
    private String strengths; // JSON 배열 문자열

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String opportunities;

    @Column(columnDefinition = "TEXT")
    private String threats;

    @Column(columnDefinition = "TEXT")
    private String ourStrategy;

    private Integer impactScore; // 1-5

    // 컬렉션 JOIN 없이 COUNT 서브쿼리로 계산 → 리스트 조회 시 N+1·메모리 페이지네이션 방지
    @Formula("(SELECT COUNT(*) FROM battle_card_articles WHERE battle_card_id = id)")
    private int sourceArticleCount;

    @OneToMany(mappedBy = "battleCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BattleCardArticle> sourceArticles = new ArrayList<>();

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
