package com.aimsp.intelligence.domain.radar;

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
@Table(name = "radar_weekly_brief")
@Getter
@Setter
@NoArgsConstructor
public class RadarWeeklyBrief {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String playerMoves;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String partnershipChanges;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String deliveryModelChanges;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pricingChanges;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String agenticOperationsChanges;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String koreaImpact;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
