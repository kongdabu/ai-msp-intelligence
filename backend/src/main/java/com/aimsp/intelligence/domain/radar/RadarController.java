package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.dto.RadarDto;
import com.aimsp.intelligence.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/radar")
@RequiredArgsConstructor
public class RadarController {

    private final RadarService radarService;
    private final RadarCollectionJobService radarCollectionJobService;

    @GetMapping("/overview")
    public ResponseEntity<RadarDto.OverviewResponse> getOverview() {
        return ResponseEntity.ok(radarService.getOverview());
    }

    @GetMapping("/signals")
    public ResponseEntity<PageResponseDto<RadarDto.SignalResponse>> getSignals(
            @RequestParam(required = false) String lens,
            @RequestParam(required = false) Integer minimumImpactScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(radarService.getSignals(lens, minimumImpactScore, page, size));
    }

    @GetMapping("/players")
    public ResponseEntity<List<RadarDto.PlayerResponse>> getPlayers() {
        return ResponseEntity.ok(radarService.getPlayers());
    }

    @PutMapping("/players/{id}")
    public ResponseEntity<RadarDto.PlayerResponse> updatePlayer(@PathVariable Long id,
                                                                  @Valid @RequestBody RadarDto.PlayerUpdateRequest request) {
        return ResponseEntity.ok(radarService.updatePlayer(id, request));
    }

    @PostMapping("/signals")
    public ResponseEntity<RadarDto.SignalResponse> registerSignal(@Valid @RequestBody RadarDto.SignalRequest request) {
        return ResponseEntity.ok(radarService.registerSignal(request));
    }

    @PostMapping("/weekly-briefs/generate")
    public ResponseEntity<RadarDto.WeeklyBriefResponse> generateWeeklyBrief() {
        return ResponseEntity.ok(radarService.generateWeeklyBrief());
    }

    @PostMapping("/collect")
    public ResponseEntity<RadarCollectionJobService.JobStatus> collectRadarSignals() {
        return ResponseEntity.accepted().body(radarCollectionJobService.start());
    }

    @GetMapping("/collect/status")
    public ResponseEntity<RadarCollectionJobService.JobStatus> getCollectionStatus() {
        return ResponseEntity.ok(radarCollectionJobService.getStatus());
    }
}
