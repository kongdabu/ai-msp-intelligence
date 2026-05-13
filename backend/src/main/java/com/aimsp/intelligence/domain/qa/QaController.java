package com.aimsp.intelligence.domain.qa;

import com.aimsp.intelligence.dto.QaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @PostMapping("/sessions")
    public ResponseEntity<QaDto.SessionResponse> createSession() {
        return ResponseEntity.ok(qaService.createSession());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<QaDto.SessionResponse>> getSessions() {
        return ResponseEntity.ok(qaService.getSessions());
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<List<QaDto.MessageResponse>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(qaService.getMessages(id));
    }

    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<QaDto.MessageResponse> ask(
            @PathVariable Long id,
            @RequestBody QaDto.AskRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(qaService.ask(id, request.question().trim()));
    }
}
