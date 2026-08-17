package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.config.TaskExecutionLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RadarSourceVerificationService {

    public static final String SOURCE_UNAVAILABLE = "SOURCE_UNAVAILABLE";

    private final RadarSignalRepository radarSignalRepository;
    private final RadarSourceVerifier radarSourceVerifier;

    @Scheduled(initialDelayString = "${app.radar.source-verification-initial-delay-ms:10000}",
            fixedDelayString = "${app.radar.source-verification-interval-ms:3600000}")
    public void verifyRecentSources() {
        TaskExecutionLogger.logStart(log, "정기 배치: Radar 원문 출처 검증");
        int unavailableCount = verifySources();
        log.info("[Radar 원문 출처 검증] 삭제·만료 원문 {}건 제외", unavailableCount);
    }

    @Transactional
    public int verifySources() {
        int unavailableCount = 0;
        for (RadarSignal signal : radarSignalRepository.findTop20ByStatusNotOrderByCapturedAtDesc(SOURCE_UNAVAILABLE)) {
            RadarSourceVerifier.SourceCheckResult result = radarSourceVerifier.check(signal.getSourceUrl());
            if (result.status() == RadarSourceVerifier.CheckStatus.UNAVAILABLE) {
                signal.setStatus(SOURCE_UNAVAILABLE);
                unavailableCount++;
            } else if (result.status() == RadarSourceVerifier.CheckStatus.AVAILABLE) {
                signal.setSourceVerifiedAt(LocalDateTime.now());
            }
        }
        return unavailableCount;
    }
}
