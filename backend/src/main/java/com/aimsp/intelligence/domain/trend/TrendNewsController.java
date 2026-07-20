package com.aimsp.intelligence.domain.trend;

import com.aimsp.intelligence.dto.TrendNewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class TrendNewsController {

    private final TrendNewsService trendNewsService;

    @GetMapping
    public ResponseEntity<List<TrendNewsDto.Response>> getTrendNews() {
        return ResponseEntity.ok(trendNewsService.getLatestTrendNews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrendNewsDto.DetailResponse> getTrendNewsDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trendNewsService.getTrendNews(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<TrendNewsDto.Response>> generateTrendNews() {
        return ResponseEntity.ok(trendNewsService.generateTrendNews());
    }
}
