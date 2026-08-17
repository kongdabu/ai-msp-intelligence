package com.aimsp.intelligence.domain.strategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "strategy_report")
@Getter
@Setter
@NoArgsConstructor
public class StrategyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valueChainImpact;

    @Column(columnDefinition = "TEXT")
    private String fdeDeliveryAnalysis;

    @Column(columnDefinition = "TEXT")
    private String pricingModelAnalysis;

    @Column(columnDefinition = "TEXT")
    private String agenticOpsAnalysis;

    @Column(columnDefinition = "TEXT")
    private String mspOpportunitiesThreats;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String top3Actions;

    @Column(nullable = false)
    private int sourceSignalCount;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
