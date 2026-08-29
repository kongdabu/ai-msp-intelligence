package com.aimsp.intelligence.domain.radarreport;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Radar 보고서 신호를 뒷받침하는 출처다. */
@Entity
@Table(name = "radar_report_source")
@Getter
@Setter
@NoArgsConstructor
public class RadarReportSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "signal_id", nullable = false)
    private RadarReportSignal signal;

    @Column(nullable = false, length = 200)
    private String publisher;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 2000)
    private String url;

    private LocalDate publishedDate;

    @Column(nullable = false, length = 80)
    private String sourceType;
}
