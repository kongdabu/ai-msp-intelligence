package com.aimsp.intelligence.config;

import com.aimsp.intelligence.crawler.CrawlerOrchestrator;
import com.aimsp.intelligence.domain.insight.InsightService;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final CrawlerOrchestrator crawlerOrchestrator;
    private final InsightService insightService;

    /**
     * 기사 수집 - 매일 KST 01:00 (UTC 16:00)
     * zone = "Asia/Seoul" 로 설정해 서버 타임존(UTC)과 무관하게 한국시간 기준 실행
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void scheduledCrawl() {
        log.info("[스케줄] 기사 수집 시작 (KST 01:00)");
        try {
            int count = crawlerOrchestrator.crawlAll();
            log.info("[스케줄] 기사 수집 완료: {}건", count);
        } catch (AiApiUnavailableException e) {
            log.error("[스케줄] 기사 수집 중단 - Gemini API 비정상: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[스케줄] 기사 수집 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 인사이트 생성 - 매일 KST 02:00 (UTC 17:00)
     * 크롤링(01:00) 완료 후 1시간 뒤 실행
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void scheduledInsightGeneration() {
        log.info("[스케줄] 인사이트 생성 시작 (KST 02:00)");
        try {
            int count = insightService.generateInsights().size();
            log.info("[스케줄] 인사이트 생성 완료: {}건", count);
        } catch (AiApiUnavailableException e) {
            log.error("[스케줄] 인사이트 생성 중단 - Gemini API 비정상: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[스케줄] 인사이트 생성 실패: {}", e.getMessage(), e);
        }
    }
}
