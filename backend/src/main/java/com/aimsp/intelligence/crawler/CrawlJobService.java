package com.aimsp.intelligence.crawler;

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
public class CrawlJobService {

    private final CrawlerOrchestrator crawlerOrchestrator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile JobStatus latestStatus = JobStatus.idle();

    public synchronized JobStatus start() {
        if (!running.compareAndSet(false, true)) return latestStatus;

        LocalDateTime startedAt = LocalDateTime.now();
        latestStatus = new JobStatus("RUNNING", startedAt, null, null, null);
        executor.submit(() -> {
            try {
                log.info("[백그라운드 수집] 시작: AI 생태계·사업모델 기사 수집");
                int savedCount = crawlerOrchestrator.crawlAll();
                latestStatus = new JobStatus("COMPLETED", startedAt, LocalDateTime.now(), savedCount, null);
                log.info("[백그라운드 수집] 완료: 신규 {}건", savedCount);
            } catch (Exception e) {
                latestStatus = new JobStatus("FAILED", startedAt, LocalDateTime.now(), null, "수집 작업 중 오류가 발생했습니다.");
                log.error("[백그라운드 수집] 실패: {}", e.getMessage(), e);
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
                            Integer savedCount, String message) {
        private static JobStatus idle() {
            return new JobStatus("IDLE", null, null, null, null);
        }
    }
}
