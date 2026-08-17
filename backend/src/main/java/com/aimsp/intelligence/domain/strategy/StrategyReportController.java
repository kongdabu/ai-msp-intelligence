package com.aimsp.intelligence.domain.strategy;

import com.aimsp.intelligence.dto.PageResponseDto;
import com.aimsp.intelligence.dto.StrategyReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategy-reports")
@RequiredArgsConstructor
public class StrategyReportController {

    private final StrategyReportService strategyReportService;

    @GetMapping
    public ResponseEntity<PageResponseDto<StrategyReportDto.Response>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return ResponseEntity.ok(strategyReportService.getReports(PageRequest.of(safePage, safeSize)));
    }

    @GetMapping("/latest")
    public ResponseEntity<StrategyReportDto.Response> getLatestReport() {
        StrategyReportDto.Response latest = strategyReportService.getLatestReport();
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StrategyReportDto.Response> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(strategyReportService.getReport(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<StrategyReportDto.Response> generateReport() {
        return ResponseEntity.ok(strategyReportService.generateReport());
    }
}
