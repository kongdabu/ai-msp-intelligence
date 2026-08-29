package com.aimsp.intelligence.domain.radarreport;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 외부 AI Services Radar 작업이 생성한 일자별 보고서 원본이다. */
@Entity
@Table(name = "radar_report", uniqueConstraints = @UniqueConstraint(
        name = "uk_radar_report_type_date", columnNames = {"report_type", "report_date"}))
@Getter
@Setter
@NoArgsConstructor
public class RadarReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "report_type", nullable = false, length = 80)
    private String reportType;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String executiveView;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String strategicInterpretation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String markdown;

    @Column(length = 100)
    private String promptVersion;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RadarReportSignal> signals = new ArrayList<>();
}
