package com.aimsp.intelligence.domain.config;

import com.aimsp.intelligence.dto.SystemConfigDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Transactional(readOnly = true)
    public SystemConfigDto.Response getConfig() {
        return SystemConfigDto.Response.from(getOrCreate());
    }

    @Transactional
    public SystemConfigDto.Response updateConfig(SystemConfigDto.Request request) {
        SystemConfig config = getOrCreate();
        config.setMaxArticlesForInsight(request.getMaxArticlesForInsight());
        config.setMaxInsightsPerGeneration(request.getMaxInsightsPerGeneration());
        return SystemConfigDto.Response.from(systemConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public SystemConfig getOrCreate() {
        return systemConfigRepository.findById(1L).orElseGet(() -> {
            SystemConfig defaultConfig = new SystemConfig();
            return systemConfigRepository.save(defaultConfig);
        });
    }
}
