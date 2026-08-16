package com.aimsp.intelligence.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** Gemini 비용이 드는 작업을 한 번에 하나만 실행하도록 조정한다. */
@Slf4j
@Component
public class GeminiWorkCoordinator {

    private final ReentrantLock lock = new ReentrantLock();

    public <T> T executeExclusive(String taskName, Supplier<T> task) {
        if (!lock.tryLock()) {
            throw new IllegalStateException("다른 Gemini 작업이 실행 중입니다. 현재 작업이 끝난 뒤 다시 시도해 주세요.");
        }
        try {
            log.info("Gemini 작업 시작: {}", taskName);
            return task.get();
        } finally {
            lock.unlock();
            log.info("Gemini 작업 종료: {}", taskName);
        }
    }
}
