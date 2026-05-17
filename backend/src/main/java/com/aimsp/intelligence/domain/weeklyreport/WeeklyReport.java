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
    private String competitorTrends;       // 경쟁사 동향 (JSON 텍스트)

    @Column(columnDefinition = "TEXT")
    private String aiTrends;               // AI 사업 Trend (JSON 텍스트)

    @Column(columnDefinition = "TEXT")
    private String strategyRecommendations; // 추진 전략 (JSON 텍스트)

    private LocalDate weekStart;           // 대상 주간 시작일 (월요일)
    private LocalDate weekEnd;             // 대상 주간 종료일 (일요일)

    private Integer articleCount;
    private Integer insightCount;

    @Column(length = 500)
    private String docxPath;              // 생성된 Word 파일 경로

    private LocalDateTime generatedAt;
}
