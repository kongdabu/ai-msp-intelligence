package com.aimsp.intelligence.domain.radar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "radar_assessment")
@Getter
@Setter
@NoArgsConstructor
public class RadarAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "signal_id", nullable = false, unique = true)
    private RadarSignal signal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String whatChanged;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String industryStructureImpact;

    @Column(columnDefinition = "TEXT")
    private String mspOpportunity;

    @Column(columnDefinition = "TEXT")
    private String mspThreat;

    @Column(columnDefinition = "TEXT")
    private String structuralRisk;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(length = 80)
    private String deliveryModel;

    @Column(length = 80)
    private String pricingModel;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
