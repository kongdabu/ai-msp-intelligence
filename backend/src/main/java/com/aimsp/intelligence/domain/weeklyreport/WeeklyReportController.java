package com.aimsp.intelligence.domain.weeklyreport;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
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

    // Python 스크립트가 생성 후 업로드하는 엔드포인트 (JSON + Base64)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestBody UploadRequest req) {
        try {
            byte[] content = Base64.getDecoder().decode(req.getContent());
            WeeklyReport saved = weeklyReportService.saveUploadedReport(
                    req.getTitle(), req.getWeekStart(), req.getWeekEnd(),
                    req.getArticleCount(), req.getInsightCount(), content);
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

    @Getter @Setter
    public static class UploadRequest {
        private String title;
        private String weekStart;
        private String weekEnd;
        private int articleCount;
        private int insightCount;
        private String content; // Base64 인코딩된 DOCX
    }
}
