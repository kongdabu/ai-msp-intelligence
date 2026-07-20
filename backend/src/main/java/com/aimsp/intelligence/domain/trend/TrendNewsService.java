package com.aimsp.intelligence.domain.trend;

import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.ai.TrendNewsGenerator;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.TrendNewsDto;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendNewsService {

    private static final int PERIOD_DAYS = 30;
    private static final Pattern ARTICLE_CITATION_PATTERN = Pattern.compile("\\[기사\\s*#(\\d+)]");

    private final TrendNewsRepository trendNewsRepository;
    private final ArticleRepository articleRepository;
    private final TrendNewsGenerator trendNewsGenerator;
    private final GeminiApiClient geminiApiClient;
    private final @NonNull PlatformTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public List<TrendNewsDto.Response> getLatestTrendNews() {
        return trendNewsRepository.findTop20ByOrderByGeneratedAtDescTrendScoreDesc().stream()
                .map(TrendNewsDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrendNewsDto.DetailResponse getTrendNews(Long id) {
        TrendNews trendNews = trendNewsRepository.findByIdWithArticles(id)
                .orElseThrow(() -> new IllegalArgumentException("Trend News를 찾을 수 없습니다: " + id));
        return TrendNewsDto.DetailResponse.from(trendNews, buildSourceArticlesForDetail(trendNews));
    }

    public List<TrendNewsDto.Response> generateTrendNews() {
        if (!geminiApiClient.isAvailable()) throw new AiApiUnavailableException();

        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(PERIOD_DAYS);
        List<Article> articles = articleRepository.findByCollectedAtBetweenOrderByCollectedAtDesc(periodStart, periodEnd);
        if (articles.size() < 3) {
            log.info("Trend News 생성 건너뜀: 최근 30일 기사 {}건", articles.size());
            return List.of();
        }

        List<TrendNews> trends = trendNewsGenerator.generate(articles, periodStart, periodEnd);
        if (trends.isEmpty()) return List.of();

        List<TrendNewsDto.Response> saved = new TransactionTemplate(transactionManager).execute(status -> trends.stream()
                .map(trendNewsRepository::save)
                .map(TrendNewsDto.Response::from)
                .collect(Collectors.toList()));
        log.info("Trend News {}건 생성 완료", saved != null ? saved.size() : 0);
        return saved != null ? saved : List.of();
    }

    // 기존 초안까지 포함해 본문의 [기사 #ID] 인용 순서대로 근거 링크를 노출한다.
    private List<TrendNewsDto.SourceArticleResponse> buildSourceArticlesForDetail(TrendNews trendNews) {
        Map<Long, TrendNewsArticle> linkedArticles = new LinkedHashMap<>();
        trendNews.getSourceArticles().forEach(association -> linkedArticles.put(association.getArticle().getId(), association));

        LinkedHashSet<Long> citedArticleIds = extractCitedArticleIds(trendNews.getContent());
        Map<Long, Article> articlesById = new LinkedHashMap<>();
        articleRepository.findAllById(citedArticleIds).forEach(article -> articlesById.put(article.getId(), article));

        List<TrendNewsDto.SourceArticleResponse> result = new ArrayList<>();
        for (Long articleId : citedArticleIds) {
            TrendNewsArticle linked = linkedArticles.remove(articleId);
            Article article = articlesById.get(articleId);
            if (linked != null) {
                result.add(TrendNewsDto.SourceArticleResponse.from(linked));
            } else if (article != null) {
                result.add(TrendNewsDto.SourceArticleResponse.from(article));
            }
        }
        linkedArticles.values().forEach(association -> result.add(TrendNewsDto.SourceArticleResponse.from(association)));
        return result;
    }

    private LinkedHashSet<Long> extractCitedArticleIds(String content) {
        LinkedHashSet<Long> articleIds = new LinkedHashSet<>();
        Matcher matcher = ARTICLE_CITATION_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) articleIds.add(Long.parseLong(matcher.group(1)));
        return articleIds;
    }
}
