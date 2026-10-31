package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CancellationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CrawlerOrchestratorTest {

    @Mock(mockMaker = MockMakers.SUBCLASS)
    private ArticleService articleService;

    @Mock(mockMaker = MockMakers.SUBCLASS)
    private OfficialSiteCrawler officialSiteCrawler;

    @InjectMocks
    private CrawlerOrchestrator crawlerOrchestrator;

    @Test
    @DisplayName("공식 사이트 기사는 뉴스 키워드와 무관하게 저장하고 중복과 빈 본문은 제외한다")
    void crawlAll_keepsOfficialArticlesAndSkipsInvalidCandidates() {
        Article valid = article("https://example.com/new", "공식 발표 본문");
        Article duplicate = article("https://example.com/duplicate", "중복 기사 본문");
        Article blank = article("https://example.com/blank", " ");
        Article missing = article("https://example.com/missing", null);
        given(officialSiteCrawler.crawl()).willReturn(List.of(valid, duplicate, blank, missing));
        given(articleService.existsByUrl(anyString()))
                .willAnswer(invocation -> duplicate.getUrl().equals(invocation.getArgument(0)));
        given(articleService.saveIfNotExists(valid)).willReturn(valid);

        assertThat(crawlerOrchestrator.crawlAll()).isEqualTo(1);

        verify(articleService).saveIfNotExists(valid);
        verify(articleService, never()).saveIfNotExists(duplicate);
        verify(articleService, never()).saveIfNotExists(blank);
        verify(articleService, never()).saveIfNotExists(missing);
    }

    @Test
    @DisplayName("수집 취소 시 기사 저장을 진행하지 않고 취소 상태를 전달한다")
    void crawlAll_preservesCancellation() {
        given(officialSiteCrawler.crawl()).willReturn(List.of(article("https://example.com/new", "본문")));
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(crawlerOrchestrator::crawlAll).isInstanceOf(CancellationException.class);
            verifyNoInteractions(articleService);
        } finally {
            Thread.interrupted();
        }
    }

    private Article article(String url, String content) {
        Article article = new Article();
        article.setUrl(url);
        article.setTitle("공식 발표");
        article.setOriginalContent(content);
        return article;
    }
}
