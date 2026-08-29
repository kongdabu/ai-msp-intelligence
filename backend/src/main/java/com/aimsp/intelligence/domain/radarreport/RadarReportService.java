package com.aimsp.intelligence.domain.radarreport;

import com.aimsp.intelligence.dto.PageResponseDto;
import com.aimsp.intelligence.dto.RadarReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RadarReportService {

    private final RadarReportRepository radarReportRepository;
    private final RadarReportSignalRepository radarReportSignalRepository;

    /** 같은 보고서 유형과 날짜의 재전송은 기존 보고서 전체를 원자적으로 대체한다. */
    @Transactional
    public RadarReportDto.ReportResponse upsert(RadarReportDto.UpsertRequest request) {
        LocalDateTime now = LocalDateTime.now();
        RadarReport report = radarReportRepository.findByReportTypeAndReportDate(request.reportType(), request.reportDate())
                .orElseGet(() -> createReport(request.reportDate(), request.reportType(), now));

        report.setTitle(request.title());
        report.setExecutiveView(request.executiveView());
        report.setStrategicInterpretation(request.strategicInterpretation());
        report.setMarkdown(request.markdown());
        report.setPromptVersion(request.promptVersion());
        report.setUpdatedAt(now);
        report.getSignals().clear();
        request.signals().forEach(signalRequest -> report.getSignals().add(toSignal(report, signalRequest)));

        return RadarReportDto.ReportResponse.from(radarReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public RadarReportDto.ReportResponse getByDate(LocalDate reportDate, String reportType) {
        RadarReport report = radarReportRepository.findByReportTypeAndReportDate(reportType, reportDate)
                .orElseThrow(() -> new IllegalArgumentException("Radar 보고서를 찾을 수 없습니다: " + reportType + " / " + reportDate));
        return RadarReportDto.ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<RadarReportDto.ReportResponse> getReports(String reportType, LocalDate fromDate, LocalDate toDate,
                                                                       int page, int size) {
        validateDateRange(fromDate, toDate);
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by(Sort.Order.desc("reportDate"), Sort.Order.desc("updatedAt")));
        Specification<RadarReport> specification = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (hasText(reportType)) predicates.add(cb.equal(root.get("reportType"), reportType));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("reportDate"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("reportDate"), toDate));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return PageResponseDto.from(radarReportRepository.findAll(specification, pageable).map(RadarReportDto.ReportResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponseDto<RadarReportDto.SignalResponse> getSignals(String reportType, LocalDate fromDate, LocalDate toDate,
                                                                       String company, String category, String importance,
                                                                       String queryText, int page, int size) {
        validateDateRange(fromDate, toDate);
        Pageable pageable = PageRequest.of(safePage(page), safeSize(size),
                Sort.by(Sort.Order.desc("report.reportDate"), Sort.Order.asc("company"), Sort.Order.asc("id")));
        Specification<RadarReportSignal> specification = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            var report = root.join("report");
            if (hasText(reportType)) predicates.add(cb.equal(report.get("reportType"), reportType));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(report.get("reportDate"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(report.get("reportDate"), toDate));
            if (hasText(company)) predicates.add(cb.equal(root.get("company"), company));
            if (hasText(category)) predicates.add(cb.equal(root.get("category"), category));
            if (hasText(importance)) predicates.add(cb.equal(root.get("importance"), importance));
            if (hasText(queryText)) {
                String pattern = "%" + queryText.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("company")), pattern),
                        cb.like(cb.lower(root.get("signal")), pattern),
                        cb.like(cb.lower(root.get("fact")), pattern)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return PageResponseDto.from(radarReportSignalRepository.findAll(specification, pageable)
                .map(RadarReportDto.SignalResponse::from));
    }

    private RadarReport createReport(LocalDate reportDate, String reportType, LocalDateTime now) {
        RadarReport report = new RadarReport();
        report.setReportDate(reportDate);
        report.setReportType(reportType);
        report.setCreatedAt(now);
        return report;
    }

    private RadarReportSignal toSignal(RadarReport report, RadarReportDto.SignalRequest request) {
        RadarReportSignal signal = new RadarReportSignal();
        signal.setReport(report);
        signal.setCompany(request.company());
        signal.setCategory(request.category());
        signal.setImportance(request.importance());
        signal.setSignal(request.signal());
        signal.setFact(request.fact());
        signal.setWhatChanged(request.whatChanged());
        signal.setIndustryImpact(request.industryImpact());
        signal.setOpportunity(request.opportunity());
        signal.setThreat(request.threat());
        signal.setStructuralRisk(request.structuralRisk());
        signal.setPracticalImplication(request.practicalImplication());
        signal.setRecommendedAction(request.recommendedAction());
        request.sources().forEach(sourceRequest -> signal.getSources().add(toSource(signal, sourceRequest)));
        return signal;
    }

    private RadarReportSource toSource(RadarReportSignal signal, RadarReportDto.SourceRequest request) {
        RadarReportSource source = new RadarReportSource();
        source.setSignal(signal);
        source.setPublisher(request.publisher());
        source.setTitle(request.title());
        source.setUrl(request.url());
        source.setPublishedDate(request.publishedDate());
        source.setSourceType(request.sourceType());
        return source;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 50);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
