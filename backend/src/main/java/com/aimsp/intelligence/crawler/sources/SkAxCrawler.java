package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.domain.article.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SkAxCrawler {

    // Google News RSS - "SK AX" 관련 한국 뉴스
    private static final String NEWS_RSS =
            "https://news.google.com/rss/search?q=%22SK+AX%22+OR+%22SK에이엔에스%22+OR+%22SK+클라우드%22&hl=ko&gl=KR&ceid=KR:ko";
    private static final String SOURCE_NAME = "SK AX 뉴스";
    private static final String COMPETITOR = "SK_AX";

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();
        try {
            Thread.sleep(2000);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(NEWS_RSS)));

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
