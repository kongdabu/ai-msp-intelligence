package com.aimsp.intelligence.domain.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfig {

    @Id
    private Long id = 1L; // 싱글톤 레코드

    @Column(nullable = false)
    private int maxArticlesForInsight = 150; // 인사이트 생성 시 입력 기사 최대 수

    @Column(nullable = false)
    private int maxInsightsPerGeneration = 8; // 인사이트 생성 최대 건수

    @Column(nullable = false)
    private int minRelevanceScoreForInsight = 65; // 인사이트 근거로 연결할 기사의 최소 관련도 점수
}
