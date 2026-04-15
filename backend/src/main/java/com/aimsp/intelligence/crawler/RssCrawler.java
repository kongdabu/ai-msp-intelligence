package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.source.Source;
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
public class RssCrawler {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    /**
     * RSS 피드 크롤링 후 Article 목록 반환
     */
    public List<Article> crawl(Source source) {
        List<Article> articles = new ArrayList<>();
        try {
            // OkHttp로 피드 원문 수신 후 DOCTYPE 선언 제거
            // (일부 사이트가 RSS에 비표준 DOCTYPE을 포함 → JAXP 파서가 XXE 방지로 차단)
            String xmlBody = fetchRawXml(source.getUrl());
            String sanitized = sanitizeXml(xmlBody);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(sanitized));

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

    private String fetchRawXml(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (compatible; RSSReader/1.0)")
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IllegalStateException("RSS 피드 응답 오류: HTTP " + response.code());
            }
            return response.body().string();
        }
    }

    /**
     * RSS 피드 XML 정제
     * 1) DOCTYPE 선언 제거 (JAXP XXE 방지 설정과 충돌)
     * 2) <script> 태그 전체 제거 (피드 본문에 삽입된 불필요한 스크립트)
     * 3) HTML5 불리언 속성 → XML 호환 형태로 변환
     *    예: <script async> → <script async="async">
     */
    private String sanitizeXml(String xml) {
        // 1) DOCTYPE 제거
        xml = xml.replaceAll("(?si)<!DOCTYPE[^>]*(?:\\[[^]]*])?\\s*>", "");
        // 2) <script> 블록 제거
        xml = xml.replaceAll("(?si)<script[^>]*>.*?</script>", "");
        // 3) HTML5 불리언 속성 → XML 속성값 형태로 변환 (공백 또는 /> 직전의 단독 속성명)
        xml = xml.replaceAll(
                "(?i)\\s(async|defer|checked|disabled|selected|multiple|readonly|required|autofocus|autoplay|controls|loop|muted|open|reversed)(?=[\\s/>])",
                " $1=\"$1\""
        );
        return xml;
    }
}
