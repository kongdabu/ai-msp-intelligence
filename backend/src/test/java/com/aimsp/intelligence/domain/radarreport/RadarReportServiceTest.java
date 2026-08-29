package com.aimsp.intelligence.domain.radarreport;

import com.aimsp.intelligence.dto.RadarReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RadarReportServiceTest {

    @Mock
    private RadarReportRepository radarReportRepository;

    @Mock
    private RadarReportSignalRepository radarReportSignalRepository;

    private RadarReportService radarReportService;

    @BeforeEach
    void setUp() {
        radarReportService = new RadarReportService(radarReportRepository, radarReportSignalRepository);
    }

    @Test
    @DisplayName("같은 보고서 유형과 날짜로 재전송하면 자식 신호와 출처를 포함해 기존 보고서를 갱신한다")
    void upsert_shouldReplaceExistingReportContents() {
        LocalDate reportDate = LocalDate.of(2026, 8, 29);
        RadarReport existing = new RadarReport();
        existing.setId(7L);
        existing.setReportDate(reportDate);
        existing.setReportType("AI_SERVICES_RADAR");
        existing.setCreatedAt(LocalDateTime.of(2026, 8, 29, 9, 0));
        RadarReportSignal oldSignal = new RadarReportSignal();
        oldSignal.setReport(existing);
        existing.getSignals().add(oldSignal);
        given(radarReportRepository.findByReportTypeAndReportDate("AI_SERVICES_RADAR", reportDate)).willReturn(Optional.of(existing));
        given(radarReportRepository.save(any(RadarReport.class))).willAnswer(invocation -> invocation.getArgument(0));

        RadarReportDto.ReportResponse response = radarReportService.upsert(request(reportDate, "새 보고서", "OpenAI"));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(existing.getTitle()).isEqualTo("새 보고서");
        assertThat(existing.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 8, 29, 9, 0));
        assertThat(existing.getSignals()).hasSize(1);
        RadarReportSignal signal = existing.getSignals().getFirst();
        assertThat(signal.getCompany()).isEqualTo("OpenAI");
        assertThat(signal.getReport()).isSameAs(existing);
        assertThat(signal.getSources()).hasSize(1);
        assertThat(signal.getSources().getFirst().getSignal()).isSameAs(signal);
        verify(radarReportRepository).save(existing);
    }

    @Test
    @DisplayName("신호 조회는 보고서 유형·기간·기업·카테고리·중요도 필터와 일자 내림차순 정렬을 사용한다")
    void getSignals_shouldApplyFiltersAndSortByReportDate() {
        RadarReport report = new RadarReport();
        report.setId(1L);
        report.setReportDate(LocalDate.of(2026, 8, 29));
        report.setReportType("AI_SERVICES_RADAR");
        RadarReportSignal signal = new RadarReportSignal();
        signal.setId(2L);
        signal.setReport(report);
        signal.setCompany("OpenAI");
        signal.setCategory("AI_PRICING");
        signal.setImportance("HIGH");
        signal.setSignal("가격 변화");
        signal.setFact("요금이 변경됐다");
        signal.setWhatChanged("변경 내용");
        signal.setIndustryImpact("산업 영향");
        signal.setRecommendedAction("대응 방안");
        given(radarReportSignalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(signal)));

        var result = radarReportService.getSignals("AI_SERVICES_RADAR", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31), "OpenAI", "AI_PRICING", "HIGH", "가격", 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().company()).isEqualTo("OpenAI");
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(radarReportSignalRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("report.reportDate").isDescending()).isTrue();
    }

    @Test
    @DisplayName("조회 시작일이 종료일보다 늦으면 잘못된 요청으로 처리한다")
    void getReports_shouldRejectInvalidDateRange() {
        assertThatThrownBy(() -> radarReportService.getReports(null, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 8, 1), 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작일");
    }

    private RadarReportDto.UpsertRequest request(LocalDate reportDate, String title, String company) {
        RadarReportDto.SourceRequest source = new RadarReportDto.SourceRequest("OpenAI", "공식 발표", "https://openai.com/news", reportDate, "OFFICIAL");
        RadarReportDto.SignalRequest signal = new RadarReportDto.SignalRequest(company, "AI_PRICING", "HIGH", "가격 신호",
                "사실", "변화", "산업 영향", "기회", "위협", "구조적 위험", "실무 시사점", "권고 행동", List.of(source));
        return new RadarReportDto.UpsertRequest(reportDate, "AI_SERVICES_RADAR", title, "경영진 관점", "전략 해석",
                "# Radar", "phase1-v1", List.of(signal));
    }
}
