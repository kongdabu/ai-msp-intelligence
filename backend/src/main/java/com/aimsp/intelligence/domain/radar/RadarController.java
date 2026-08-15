package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.dto.RadarDto;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/radar")
@RequiredArgsConstructor
public class RadarController {

    private final RadarService radarService;

    @GetMapping("/overview")
    public ResponseEntity<RadarDto.OverviewResponse> getOverview() {
        return ResponseEntity.ok(radarService.getOverview());
    }

    @GetMapping("/signals")
    public ResponseEntity<List<RadarDto.SignalResponse>> getSignals() {
        return ResponseEntity.ok(radarService.getSignals());
    }

    @PostMapping("/signals")
    public ResponseEntity<RadarDto.SignalResponse> registerSignal(@Valid @RequestBody RadarDto.SignalRequest request) {
        return ResponseEntity.ok(radarService.registerSignal(request));
    }

    @PostMapping("/weekly-briefs/generate")
    public ResponseEntity<RadarDto.WeeklyBriefResponse> generateWeeklyBrief() {
        return ResponseEntity.ok(radarService.generateWeeklyBrief());
    }
}
