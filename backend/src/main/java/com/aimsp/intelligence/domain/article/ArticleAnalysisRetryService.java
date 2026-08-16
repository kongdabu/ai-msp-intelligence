package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Gemini 호출 제한 이후 남은 원문을 소량씩 재분석한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAnalysisRetryService {

    private final ArticleRepository articleRepository;
    private final ArticleAnalysisService articleAnalysisService;
    private final GeminiApiClient geminiApiClient;

    public int retryPendingArticles() {
        if (!geminiApiClient.isAvailable()) {
            log.warn("Gemini API 비정상으로 대기 기사 재분석을 건너뜁니다.");
            return 0;
        }
        int completedCount = 0;
        for (Article article : articleRepository.findTop5ByAnalysisStatusOrderByCollectedAtAsc(ArticleAnalysisService.PENDING)) {
            try {
                if (articleAnalysisService.analyze(article) == ArticleAnalysisService.AnalysisOutcome.COMPLETED) completedCount++;
            } catch (AiApiUnavailableException e) {
                log.warn("Gemini 호출 제한으로 대기 기사 재분석을 중단합니다.");
                break;
            }
        }
        return completedCount;
    }
}
