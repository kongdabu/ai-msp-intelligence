package com.aimsp.intelligence.domain.weeklyreport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/weekly-reports")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    // 레포트 목록 (최근 10건)
    @GetMapping
    public ResponseEntity<List<WeeklyReportSummary>> list() {
        return ResponseEntity.ok(weeklyReportService.getRecentReportSummaries());
    }

    // Word 파일 다운로드 (DB 저장 콘텐츠 제공)
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        WeeklyReport report = weeklyReportService.getReport(id);
        if (report.getDocxContent() == null) {
            return ResponseEntity.notFound().build();
        }
        String filename = report.getWeekStart() + "_ai-msp-weekly-report.docx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(report.getDocxContent());
    }

    // Python 스크립트가 생성 후 업로드하는 엔드포인트
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam String title,
            @RequestParam String weekStart,
            @RequestParam String weekEnd,
            @RequestParam int articleCount,
            @RequestParam int insightCount,
            @RequestParam MultipartFile file) {
        try {
            WeeklyReport saved = weeklyReportService.saveUploadedReport(
                    title, weekStart, weekEnd, articleCount, insightCount, file.getBytes());
            log.info("주간 레포트 업로드 완료: id={}, title={}", saved.getId(), saved.getTitle());
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "title", saved.getTitle(),
                    "downloadUrl", "/api/weekly-reports/" + saved.getId() + "/download"
            ));
        } catch (Exception e) {
            log.error("주간 레포트 업로드 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
