package com.aimsp.intelligence.domain.radarreport;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Radar 보고서 안의 구조화된 단일 산업 신호다. 기존 수집 RadarSignal과 별도 모델이다. */
@Entity
@Table(name = "radar_report_signal")
@Getter
@Setter
@NoArgsConstructor
public class RadarReportSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private RadarReport report;

    @Column(nullable = false, length = 160)
    private String company;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 40)
    private String importance;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fact;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String whatChanged;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String industryImpact;

    @Column(columnDefinition = "TEXT")
    private String opportunity;

    @Column(columnDefinition = "TEXT")
    private String threat;

    @Column(columnDefinition = "TEXT")
    private String structuralRisk;

    @Column(columnDefinition = "TEXT")
    private String practicalImplication;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedAction;

    @OneToMany(mappedBy = "signal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RadarReportSource> sources = new ArrayList<>();
}
