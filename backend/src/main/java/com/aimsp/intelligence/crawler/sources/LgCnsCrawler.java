package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.domain.article.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LgCnsCrawler {

    private static final String NEWS_RSS =
            "https://news.google.com/rss/search?q=%22LG+CNS%22+OR+%22LGCNS%22&hl=ko&gl=KR&ceid=KR:ko";
    private static final String SOURCE_NAME = "LG CNS 뉴스";
    private static final String COMPETITOR = "LG_CNS";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();
        try {
            String body = fetchRaw(NEWS_RSS);
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
            log.info("LG CNS 뉴스 수집 완료: {}건", articles.size());
        } catch (Exception e) {
            log.error("LG CNS 뉴스 수집 실패: {}", e.getMessage());
        }
        return articles;
    }

    private String fetchRaw(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (compatible; RSSReader/1.0)")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IllegalStateException("HTTP " + response.code());
            }
            return response.body().string();
        }
    }
}
