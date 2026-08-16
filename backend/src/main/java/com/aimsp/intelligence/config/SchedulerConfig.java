package com.aimsp.intelligence.config;

import com.aimsp.intelligence.domain.battlecard.BattleCardService;
import com.aimsp.intelligence.domain.article.ArticleAnalysisRetryService;
import com.aimsp.intelligence.crawler.CrawlerOrchestrator;
import com.aimsp.intelligence.domain.insight.InsightService;
import com.aimsp.intelligence.domain.radar.RadarCollectionService;
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
    private final RadarCollectionService radarCollectionService;
    private final InsightService insightService;
    private final BattleCardService battleCardService;
    private final ArticleAnalysisRetryService articleAnalysisRetryService;
    /**
     * 원문 수집 - 매일 KST 01:00 (UTC 16:00). Gemini 분석은 별도 저빈도 배치가 수행한다.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void scheduledCrawl() {
        TaskExecutionLogger.logStart(log, "정기 배치: 원문 기사 수집");
        try {
            int count = crawlerOrchestrator.crawlAll();
            log.info("[배치: 원문 기사 수집] 완료: 신규 기사 {}건", count);
        } catch (Exception e) {
            log.error("[배치: 기사 수집] 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 인사이트 생성 - 매일 KST 02:00 (UTC 17:00)
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void scheduledInsightGeneration() {
        TaskExecutionLogger.logStart(log, "정기 배치: 인사이트 생성");
        try {
            int count = insightService.generateInsights().size();
            log.info("[배치: 인사이트] 완료: {}건", count);
        } catch (AiApiUnavailableException e) {
            log.error("[스케줄] 인사이트 생성 중단 - Gemini API 비정상: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[스케줄] 인사이트 생성 실패: {}", e.getMessage(), e);
        }
    }

    /** 공용 호출 예산으로 기사 요약과 Radar 분석을 처리 - 하루 4회 */
    @Scheduled(cron = "0 30 1,7,13,19 * * *", zone = "Asia/Seoul")
    public void scheduledAiProcessing() {
        TaskExecutionLogger.logStart(log, "정기 배치: 기사 AI 분석 및 Radar Signal 처리");
        try {
            int analyzedArticleCount = articleAnalysisRetryService.retryPendingArticles();
            RadarCollectionService.CollectionResult result = radarCollectionService.collect();
            log.info("[배치: AI 분석] 기사 {}건 완료, Radar 후보 {}건·Signal {}건 등록",
                    analyzedArticleCount, result.analyzedArticleCount(), result.savedSignalCount());
        } catch (Exception e) {
            log.error("[배치: AI 분석] 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 배틀카드 생성 - 매주 월요일 KST 03:00
     */
    @Scheduled(cron = "0 0 3 * * MON", zone = "Asia/Seoul")
    public void scheduledBattleCardGeneration() {
        TaskExecutionLogger.logStart(log, "정기 배치: 배틀카드 생성");
        try {
            int count = battleCardService.generateBattleCards().size();
            log.info("[스케줄] 배틀카드 생성 완료: {}건", count);
        } catch (AiApiUnavailableException e) {
            log.error("[스케줄] 배틀카드 생성 중단 - Gemini API 비정상: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[스케줄] 배틀카드 생성 실패: {}", e.getMessage(), e);
        }
    }

}
