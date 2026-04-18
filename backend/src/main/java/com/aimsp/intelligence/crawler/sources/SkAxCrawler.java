package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.domain.article.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkAxCrawler {

    private static final String NEWS_RSS =
            "https://news.google.com/rss/search?q=%22SK+AX%22+OR+%22SK에이엔에스%22+OR+%22SK+클라우드%22&hl=ko&gl=KR&ceid=KR:ko";
    private static final String SOURCE_NAME = "SK AX 뉴스";
    private static final String COMPETITOR = "SK_AX";

    private final GoogleNewsRssFetcher rssFetcher;

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();
        try {
            String body = rssFetcher.fetch(NEWS_RSS);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(body));

            for (SyndEntry entry : feed.getEntries()) {
                Article article = new Article();
                article.setUrl(entry.getLink());
                article.setTitle(entry.getTitle());
                article.setSourceName(SOURCE_NAME);
                article.setSourceType("NEWS");
                article.setCompetitor(COMPETITOR);

                Date pub = entry.getPublishedDate() != null ? entry.getPublishedDate() : entry.getUpdatedDate();
                article.setPublishedAt(pub != null
                        ? pub.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                        : LocalDateTime.now());

                String raw = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                article.setOriginalContent(Jsoup.parse(raw).text());

                articles.add(article);
                if (articles.size() >= 20) break;
            }
            log.info("SK AX 뉴스 수집 완료: {}건", articles.size());
        } catch (Exception e) {
            log.error("SK AX 뉴스 수집 실패: {}", e.getMessage());
        }
        return articles;
    }
}
