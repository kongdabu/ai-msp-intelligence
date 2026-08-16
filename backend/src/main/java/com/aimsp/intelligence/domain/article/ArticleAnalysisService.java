package com.aimsp.intelligence.domain.article;

import com.aimsp.intelligence.ai.SummaryGenerator;
import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 원문 저장과 분리해 Gemini 요약·분류 결과만 갱신한다. */
@Service
@RequiredArgsConstructor
public class ArticleAnalysisService {

    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String REJECTED = "REJECTED";

    private final ArticleRepository articleRepository;
    private final SummaryGenerator summaryGenerator;
    private final AppConfig appConfig;

    @Transactional
    public AnalysisOutcome analyze(Article article) {
        if (article.getOriginalContent() == null || article.getOriginalContent().isBlank()) {
            article.setAnalysisStatus(REJECTED);
            saveIfPersisted(article);
            return AnalysisOutcome.REJECTED;
        }
        try {
            SummaryGenerator.SummaryResult result = summaryGenerator.generateSummary(article.getTitle(), article.getOriginalContent());
            if (result == null) {
                article.setAnalysisStatus(PENDING);
                saveIfPersisted(article);
                return AnalysisOutcome.PENDING;
            }
            int minimumRelevanceScore = "HOMEPAGE".equals(article.getSourceType())
                    ? appConfig.getOfficialSiteMinimumRelevanceScore() : 50;
            if (result.relevanceScore() < minimumRelevanceScore) {
                article.setAnalysisStatus(REJECTED);
                saveIfPersisted(article);
                return AnalysisOutcome.REJECTED;
            }
            article.setSummary(result.summary());
            article.setRelevanceScore(result.relevanceScore());
            if (article.getCategory() == null) article.setCategory(result.detectedCategory());
            article.setAnalysisStatus(COMPLETED);
            saveIfPersisted(article);
            return AnalysisOutcome.COMPLETED;
        } catch (AiApiUnavailableException e) {
            article.setAnalysisStatus(PENDING);
            saveIfPersisted(article);
            throw e;
        }
    }

    public enum AnalysisOutcome {
        COMPLETED,
        PENDING,
        REJECTED
    }

    private void saveIfPersisted(Article article) {
        if (article.getId() != null) articleRepository.save(article);
    }
}
