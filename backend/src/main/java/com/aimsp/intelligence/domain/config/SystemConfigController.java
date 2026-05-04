package com.aimsp.intelligence.domain.config;

import com.aimsp.intelligence.dto.SystemConfigDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<SystemConfigDto.Response> getConfig() {
        return ResponseEntity.ok(systemConfigService.getConfig());
    }

    @PutMapping
    public ResponseEntity<SystemConfigDto.Response> updateConfig(@RequestBody SystemConfigDto.Request request) {
        return ResponseEntity.ok(systemConfigService.updateConfig(request));
    }
}
