package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.crawler.sources.AiEcosystemCrawler;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleAnalysisService;
import com.aimsp.intelligence.domain.article.ArticleService;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.CancellationException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlerOrchestrator {

    private static final List<String> TARGET_KEYWORDS = List.of(
            "frontier ai", "openai", "anthropic", "gemini", "aws", "microsoft", "엔비디아", "nvidia",
            "accenture", "딜로이트", "deloitte", "pwc", "fde", "rde", "ode", "partnership", "파트너십",
            "agentic ai", "agentic", "aiops", "ai ops", "forward deployed", "resident deployed",
            "ai pricing model", "ai pricing", "usage-based pricing", "outcome-based pricing", "value-based pricing", "ai monetization",
            "ai 인력 양성", "ai 인재", "ai 교육", "ai 리스킬링", "ai workforce", "ai talent", "ai training", "ai upskilling", "ai reskilling",
            "ai 価格モデル", "ai 人材育成", "ai リスキリング", "生成ai 人材育成"
    );

    private final ArticleService articleService;
    private final ArticleAnalysisService articleAnalysisService;
    private final GeminiApiClient geminiApiClient;
    private final AiEcosystemCrawler aiEcosystemCrawler;
    private final OfficialSiteCrawler officialSiteCrawler;

    @SuppressWarnings("null")
    public int crawlAll() {
        boolean aiAvailable = geminiApiClient.isAvailable();
        if (!aiAvailable) log.warn("Gemini API를 사용할 수 없어 원문 수집만 수행하고 AI 요약·분류는 보류합니다.");

        int totalSaved = 0;

        totalSaved += crawlAndSave(officialSiteCrawler.crawl(), "공식 사이트", true, aiAvailable);
        totalSaved += crawlAndSave(aiEcosystemCrawler.crawl(), "AI 생태계·사업모델 뉴스", false,
                aiAvailable && !geminiApiClient.isCoolingDown());

        log.info("=== 크롤링 완료: 총 {}건 저장 ===", totalSaved);
        return totalSaved;
    }

    private int crawlAndSave(List<Article> articles, String sourceName, boolean officialSource, boolean aiAvailable) {
        int saved = 0;
        int skipped = 0;
        int preFiltered = 0;
        for (Article article : articles) {
            try {
                if (Thread.currentThread().isInterrupted()) throw new CancellationException("수집 작업이 취소되었습니다.");
                if (!officialSource && !matchesTargetKeyword(article)) {
                    preFiltered++;
                    continue;
                }
                if (articleService.existsByUrl(article.getUrl())) {
                    skipped++;
                    continue;
                }

                if (aiAvailable && article.getOriginalContent() != null && !article.getOriginalContent().isBlank()) {
                    try {
                        if (articleAnalysisService.analyze(article) == ArticleAnalysisService.AnalysisOutcome.REJECTED) {
                            skipped++;
                            continue;
                        }
                    } catch (AiApiUnavailableException e) {
                        // 원문은 보존하고 별도 재분석 배치가 처리한다.
                        article.setAnalysisStatus(ArticleAnalysisService.PENDING);
                        aiAvailable = false;
                        log.warn("Gemini 제한으로 원문만 저장하고 재분석 대기 처리: {}", article.getTitle());
                    }
                } else if (officialSource && (article.getOriginalContent() == null || article.getOriginalContent().isBlank())) {
                    skipped++;
                    continue;
                }

                Article savedArticle = articleService.saveIfNotExists(article);
                if (savedArticle != null) saved++;
            } catch (CancellationException e) {
                throw e;
            } catch (Exception e) {
                log.error("기사 저장 실패 [{}]: {}", article.getTitle(), e.getMessage());
            }
        }
        log.info("[{}] 신규 {}건 저장, 사전 필터 {}건·중복 {}건 제외", sourceName, saved, preFiltered, skipped);
        return saved;
    }

    private boolean matchesTargetKeyword(Article article) {
        String text = ((article.getTitle() == null ? "" : article.getTitle()) + " "
                + (article.getOriginalContent() == null ? "" : article.getOriginalContent()))
                .toLowerCase(Locale.ROOT);
        return TARGET_KEYWORDS.stream().anyMatch(text::contains);
    }

}
