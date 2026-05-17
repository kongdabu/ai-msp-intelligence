package com.aimsp.intelligence.domain.weeklyreport;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_report", indexes = {
    @Index(name = "idx_weekly_report_week_start", columnList = "week_start")
})
@Getter @Setter @NoArgsConstructor
public class WeeklyReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String competitorTrends;

    @Column(columnDefinition = "TEXT")
    private String aiTrends;

    @Column(columnDefinition = "TEXT")
    private String strategyRecommendations;

    private LocalDate weekStart;
    private LocalDate weekEnd;

    private Integer articleCount;
    private Integer insightCount;

    @Column(length = 500)
    private String docxPath;

    @Column(columnDefinition = "BYTEA")
    private byte[] docxContent;             // Word 파일 바이너리 (DB 저장, 원격 다운로드용)

    private LocalDateTime generatedAt;
}
