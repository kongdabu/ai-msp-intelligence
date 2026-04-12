package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.source.Source;
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
public class RssCrawler {

    /**
     * RSS 피드 크롤링 후 Article 목록 반환
     */
    public List<Article> crawl(Source source) {
        List<Article> articles = new ArrayList<>();
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(source.getUrl())));

            for (SyndEntry entry : feed.getEntries()) {
                Article article = new Article();
                article.setUrl(entry.getLink());
                article.setTitle(entry.getTitle());
                article.setSourceName(source.getName());
                article.setSourceType(source.getType());
                article.setCompetitor(source.getCompetitor());

                // 발행일 변환
                Date publishedDate = entry.getPublishedDate() != null
                        ? entry.getPublishedDate()
                        : entry.getUpdatedDate();
                if (publishedDate != null) {
                    article.setPublishedAt(publishedDate.toInstant()
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .toLocalDateTime());
                } else {
                    article.setPublishedAt(LocalDateTime.now());
                }

                // 본문 추출 (HTML 태그 제거)
                String rawContent = "";
                if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                    rawContent = entry.getContents().get(0).getValue();
                } else if (entry.getDescription() != null) {
                    rawContent = entry.getDescription().getValue();
                }
                String plainText = Jsoup.parse(rawContent).text();
                article.setOriginalContent(plainText.length() > 5000
                        ? plainText.substring(0, 5000) : plainText);

                articles.add(article);
            }
            log.info("RSS 크롤링 완료: {} → {}건", source.getName(), articles.size());
        } catch (Exception e) {
            log.error("RSS 크롤링 실패 [{}]: {}", source.getName(), e.getMessage());
        }
        return articles;
    }
}
