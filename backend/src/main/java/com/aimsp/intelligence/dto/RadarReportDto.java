package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.radarreport.RadarReport;
import com.aimsp.intelligence.domain.radarreport.RadarReportSignal;
import com.aimsp.intelligence.domain.radarreport.RadarReportSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class RadarReportDto {

    private RadarReportDto() {
    }

    public record UpsertRequest(
            @NotNull @PastOrPresent LocalDate reportDate,
            @NotBlank @Size(max = 80) String reportType,
            @NotBlank @Size(max = 300) String title,
            @NotBlank String executiveView,
            @NotBlank String strategicInterpretation,
            @NotBlank String markdown,
            @Size(max = 100) String promptVersion,
            @NotEmpty List<@NotNull @Valid SignalRequest> signals
    ) {
    }

    public record SignalRequest(
            @NotBlank @Size(max = 160) String company,
            @NotBlank @Size(max = 80) String category,
            @NotBlank @Size(max = 40) String importance,
            @NotBlank String signal,
            @NotBlank String fact,
            @NotBlank String whatChanged,
            @NotBlank String industryImpact,
            String opportunity,
            String threat,
            String structuralRisk,
            String practicalImplication,
            @NotBlank String recommendedAction,
            @NotEmpty List<@NotNull @Valid SourceRequest> sources
    ) {
    }

    public record SourceRequest(
            @NotBlank @Size(max = 200) String publisher,
            @NotBlank @Size(max = 500) String title,
            @NotBlank @Size(max = 2000) String url,
            @PastOrPresent LocalDate publishedDate,
            @NotBlank @Size(max = 80) String sourceType
    ) {
    }

    public record ReportResponse(
            Long id,
            LocalDate reportDate,
            String reportType,
            String title,
            String executiveView,
            String strategicInterpretation,
            String markdown,
            String promptVersion,
            List<SignalResponse> signals,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static ReportResponse from(RadarReport report) {
            return new ReportResponse(report.getId(), report.getReportDate(), report.getReportType(), report.getTitle(),
                    report.getExecutiveView(), report.getStrategicInterpretation(), report.getMarkdown(), report.getPromptVersion(),
                    report.getSignals().stream().map(SignalResponse::from).toList(), report.getCreatedAt(), report.getUpdatedAt());
        }
    }

    public record SignalResponse(
            Long id,
            Long reportId,
            LocalDate reportDate,
            String reportType,
            String company,
            String category,
            String importance,
            String signal,
            String fact,
            String whatChanged,
            String industryImpact,
            String opportunity,
            String threat,
            String structuralRisk,
            String practicalImplication,
            String recommendedAction,
            List<SourceResponse> sources
    ) {
        public static SignalResponse from(RadarReportSignal signal) {
            RadarReport report = signal.getReport();
            return new SignalResponse(signal.getId(), report.getId(), report.getReportDate(), report.getReportType(),
                    signal.getCompany(), signal.getCategory(), signal.getImportance(), signal.getSignal(), signal.getFact(),
                    signal.getWhatChanged(), signal.getIndustryImpact(), signal.getOpportunity(), signal.getThreat(),
                    signal.getStructuralRisk(), signal.getPracticalImplication(), signal.getRecommendedAction(),
                    signal.getSources().stream().map(SourceResponse::from).toList());
        }
    }

    public record SourceResponse(Long id, String publisher, String title, String url, LocalDate publishedDate, String sourceType) {
        public static SourceResponse from(RadarReportSource source) {
            return new SourceResponse(source.getId(), source.getPublisher(), source.getTitle(), source.getUrl(),
                    source.getPublishedDate(), source.getSourceType());
        }
    }
}
