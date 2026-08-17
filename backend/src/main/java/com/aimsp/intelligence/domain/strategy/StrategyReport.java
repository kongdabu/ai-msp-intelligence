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

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "executive_summary", nullable = false, columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(name = "value_chain_impact", nullable = false, columnDefinition = "TEXT")
    private String valueChainImpact;

    @Column(name = "fde_delivery_analysis", columnDefinition = "TEXT")
    private String fdeDeliveryAnalysis;

    @Column(name = "pricing_model_analysis", columnDefinition = "TEXT")
    private String pricingModelAnalysis;

    @Column(name = "agentic_ops_analysis", columnDefinition = "TEXT")
    private String agenticOpsAnalysis;

    @Column(name = "msp_opportunities_threats", columnDefinition = "TEXT")
    private String mspOpportunitiesThreats;

    @Column(name = "top3_actions", nullable = false, columnDefinition = "TEXT")
    private String top3Actions;

    @Column(name = "source_signal_count", nullable = false)
    private int sourceSignalCount;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
