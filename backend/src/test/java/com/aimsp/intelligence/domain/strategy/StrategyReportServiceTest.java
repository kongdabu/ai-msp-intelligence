package com.aimsp.intelligence.domain.strategy;

import com.aimsp.intelligence.ai.GeminiWorkCoordinator;
import com.aimsp.intelligence.ai.StrategyReportGenerator;
import com.aimsp.intelligence.domain.radar.RadarSignal;
import com.aimsp.intelligence.domain.radar.RadarSignalRepository;
import com.aimsp.intelligence.dto.StrategyReportDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StrategyReportServiceTest {

    @Mock
    private StrategyReportRepository strategyReportRepository;

    @Mock
    private RadarSignalRepository radarSignalRepository;

    private StrategyReportService strategyReportService;

    @BeforeEach
    void setUp() {
        StrategyReportGenerator stubGenerator = new StrategyReportGenerator(null, new ObjectMapper()) {
            @Override
            public ReportResult generateReport(List<RadarSignal> signals, LocalDateTime periodStart, LocalDateTime periodEnd) {
                return new ReportResult(
                        "2026년 8월 전략 보고서",
                        "경영진 요약",
                        "밸류체인 재편 분석",
                        "FDE 딜리버리 분석",
                        "Pricing 분석",
                        "Agentic Ops 분석",
                        "MSP 기회/위협",
                        "1. Action 1\n2. Action 2\n3. Action 3"
                );
            }
        };

        strategyReportService = new StrategyReportService(
                strategyReportRepository,
                radarSignalRepository,
                stubGenerator,
                new GeminiWorkCoordinator()
        );
    }

    @Test
    @DisplayName("Radar 신호들을 바탕으로 신규 전략 보고서가 정상 생성된다")
    void generateReport_success() {
        // given
        RadarSignal signal = new RadarSignal();
        signal.setId(1L);
        signal.setTitle("OpenAI Batch API 및 단가 인하");
        signal.setFact("OpenAI가 Batch API를 발표함");
        signal.setOccurredAt(LocalDateTime.now().minusDays(3));
        signal.setLenses(Set.of("AI_PRICING"));
        signal.setPlayers(Set.of());
        signal.setStatus("NEW");

        given(radarSignalRepository.findByOccurredAtBetweenOrderByImpactScoreDescOccurredAtDesc(any(), any()))
                .willReturn(List.of(signal));

        StrategyReport savedReport = new StrategyReport();
        savedReport.setId(100L);
        savedReport.setTitle("2026년 8월 전략 보고서");
        savedReport.setPeriodStart(LocalDateTime.now().minusDays(14));
        savedReport.setPeriodEnd(LocalDateTime.now());
        savedReport.setExecutiveSummary("경영진 요약");
        savedReport.setValueChainImpact("밸류체인 재편 분석");
        savedReport.setTop3Actions("1. Action 1\n2. Action 2\n3. Action 3");
        savedReport.setSourceSignalCount(1);
        savedReport.setGeneratedAt(LocalDateTime.now());

        given(strategyReportRepository.save(any())).willReturn(savedReport);

        // when
        StrategyReportDto.Response response = strategyReportService.generateReport();

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("2026년 8월 전략 보고서");
        assertThat(response.top3Actions()).contains("Action 1");
        verify(strategyReportRepository).save(any());
    }
}
