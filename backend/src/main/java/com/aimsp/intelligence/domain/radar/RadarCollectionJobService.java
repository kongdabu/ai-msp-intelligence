package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.config.TaskExecutionLogger;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class RadarCollectionJobService {

    private final RadarCollectionService radarCollectionService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile JobStatus latestStatus = JobStatus.idle();

    public synchronized JobStatus start() {
        if (!running.compareAndSet(false, true)) return latestStatus;

        LocalDateTime startedAt = LocalDateTime.now();
        latestStatus = new JobStatus("RUNNING", startedAt, null, null, null, null, null);
        executor.submit(() -> {
            try {
                TaskExecutionLogger.logStart(log, "백그라운드 작업: AI Services Industry Radar 수집");
                RadarCollectionService.CollectionResult result = radarCollectionService.collect();
                latestStatus = new JobStatus("COMPLETED", startedAt, LocalDateTime.now(), result.collectedArticleCount(),
                        result.analyzedArticleCount(), result.savedSignalCount(), null);
            } catch (Exception e) {
                log.error("[Radar 수집] 실패: {}", e.getMessage(), e);
                latestStatus = new JobStatus("FAILED", startedAt, LocalDateTime.now(), null, null, null,
                        "Radar 수집 작업 중 오류가 발생했습니다.");
            } finally {
                running.set(false);
            }
        });
        return latestStatus;
    }

    public JobStatus getStatus() {
        return latestStatus;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public record JobStatus(String status, LocalDateTime startedAt, LocalDateTime completedAt,
                            Integer collectedArticleCount, Integer analyzedArticleCount,
                            Integer savedSignalCount, String message) {
        private static JobStatus idle() {
            return new JobStatus("IDLE", null, null, null, null, null, null);
        }
    }
}
