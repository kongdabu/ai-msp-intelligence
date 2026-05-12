package com.aimsp.intelligence.domain.battlecard;

import com.aimsp.intelligence.dto.BattleCardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/battlecards")
@RequiredArgsConstructor
public class BattleCardController {

    private final BattleCardService battleCardService;

    // 경쟁사별 최신 배틀카드 목록
    @GetMapping
    public ResponseEntity<List<BattleCardDto.Response>> getLatestBattleCards() {
        return ResponseEntity.ok(battleCardService.getLatestBattleCards());
    }

    // 특정 경쟁사 배틀카드 이력 (최근 10건)
    @GetMapping("/{competitor}")
    public ResponseEntity<List<BattleCardDto.Response>> getBattleCardsByCompetitor(
            @PathVariable String competitor) {
        return ResponseEntity.ok(battleCardService.getBattleCardsByCompetitor(competitor));
    }

    // 배틀카드 단건 상세 — 출처 기사 포함 (/{competitor}와 경로 충돌 방지)
    @GetMapping("/detail/{id}")
    public ResponseEntity<BattleCardDto.DetailResponse> getBattleCardDetail(@PathVariable Long id) {
        return ResponseEntity.ok(battleCardService.getBattleCardDetail(id));
    }

    // 수동 배틀카드 생성
    @PostMapping("/generate")
    public ResponseEntity<List<BattleCardDto.Response>> generateBattleCards() {
        return ResponseEntity.ok(battleCardService.generateBattleCards());
    }
}
