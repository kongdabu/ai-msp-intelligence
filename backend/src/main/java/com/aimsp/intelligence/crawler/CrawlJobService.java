package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.config.TaskExecutionLogger;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlJobService {

    private final CrawlerOrchestrator crawlerOrchestrator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private volatile JobStatus latestStatus = JobStatus.idle();
    private volatile Future<?> runningTask;

    public synchronized JobStatus start() {
        if (!running.compareAndSet(false, true)) return latestStatus;

        LocalDateTime startedAt = LocalDateTime.now();
        cancellationRequested.set(false);
        latestStatus = new JobStatus("RUNNING", startedAt, null, null, null);
        runningTask = executor.submit(() -> {
            try {
                TaskExecutionLogger.logStart(log, "백그라운드 작업: 기사 수집");
                int savedCount = crawlerOrchestrator.crawlAll();
                latestStatus = cancellationRequested.get()
                        ? new JobStatus("CANCELLED", startedAt, LocalDateTime.now(), null, "사용자가 수집 작업을 취소했습니다.")
                        : new JobStatus("COMPLETED", startedAt, LocalDateTime.now(), savedCount, null);
                log.info("[백그라운드 수집] 완료: 신규 {}건", savedCount);
            } catch (CancellationException e) {
                latestStatus = new JobStatus("CANCELLED", startedAt, LocalDateTime.now(), null, "사용자가 수집 작업을 취소했습니다.");
                log.info("[백그라운드 수집] 취소됨");
            } catch (Exception e) {
                if (cancellationRequested.get() || Thread.currentThread().isInterrupted()) {
                    latestStatus = new JobStatus("CANCELLED", startedAt, LocalDateTime.now(), null, "사용자가 수집 작업을 취소했습니다.");
                    log.info("[백그라운드 수집] 취소됨");
                } else {
                    latestStatus = new JobStatus("FAILED", startedAt, LocalDateTime.now(), null, "수집 작업 중 오류가 발생했습니다.");
                    log.error("[백그라운드 수집] 실패: {}", e.getMessage(), e);
                }
            } finally {
                running.set(false);
                runningTask = null;
            }
        });
        return latestStatus;
    }

    public JobStatus getStatus() {
        return latestStatus;
    }

    public synchronized JobStatus cancel() {
        if (!running.get() || runningTask == null) return latestStatus;
        cancellationRequested.set(true);
        latestStatus = new JobStatus("CANCELLING", latestStatus.startedAt(), null, null, "수집 작업 취소를 요청했습니다.");
        runningTask.cancel(true);
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
