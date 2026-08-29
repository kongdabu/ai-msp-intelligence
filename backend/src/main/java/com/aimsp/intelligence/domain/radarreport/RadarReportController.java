package com.aimsp.intelligence.domain.radarreport;

import com.aimsp.intelligence.dto.PageResponseDto;
import com.aimsp.intelligence.dto.RadarReportDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/radar")
@RequiredArgsConstructor
public class RadarReportController {

    private static final String DEFAULT_REPORT_TYPE = "AI_SERVICES_RADAR";

    private final RadarReportService radarReportService;

    @PostMapping("/reports")
    public ResponseEntity<RadarReportDto.ReportResponse> upsertReport(@Valid @RequestBody RadarReportDto.UpsertRequest request) {
        return ResponseEntity.ok(radarReportService.upsert(request));
    }

    @GetMapping("/reports/{date}")
    public ResponseEntity<RadarReportDto.ReportResponse> getReport(
            @PathVariable LocalDate date,
            @RequestParam(defaultValue = DEFAULT_REPORT_TYPE) String reportType) {
        return ResponseEntity.ok(radarReportService.getByDate(date, reportType));
    }

    @GetMapping("/reports")
    public ResponseEntity<PageResponseDto<RadarReportDto.ReportResponse>> getReports(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(radarReportService.getReports(reportType, fromDate, toDate, page, size));
    }

    @GetMapping("/signals")
    public ResponseEntity<PageResponseDto<RadarReportDto.SignalResponse>> getSignals(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String importance,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(radarReportService.getSignals(reportType, fromDate, toDate, company, category,
                importance, query, page, size));
    }
}
