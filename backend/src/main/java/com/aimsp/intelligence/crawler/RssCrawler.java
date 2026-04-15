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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RssCrawler {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    /**
     * RSS 피드 크롤링 후 Article 목록 반환.
     * 1차: Rome (strict XML) 파싱 시도
     * 2차: XML 파싱 실패 시 Jsoup HTML 파서로 폴백 (비표준 HTML 혼재 피드 대응)
     */
    public List<Article> crawl(Source source) {
        String rawBody;
        try {
            rawBody = fetchRaw(source.getUrl());
        } catch (Exception e) {
            log.error("RSS 피드 수신 실패 [{}]: {}", source.getName(), e.getMessage());
            return List.of();
        }

        // 1차: Rome strict XML 파싱
        try {
            String sanitized = sanitizeXml(rawBody);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(sanitized));
            List<Article> articles = toArticles(feed.getEntries(), source);
            log.info("RSS 크롤링 완료 (XML): {} → {}건", source.getName(), articles.size());
            return articles;
        } catch (Exception xmlEx) {
            log.warn("RSS XML 파싱 실패 [{}], Jsoup 폴백 시도: {}", source.getName(), xmlEx.getMessage());
        }

        // 2차: Jsoup HTML 파서 폴백 (malformed XML 대응)
        try {
            List<Article> articles = parseWithJsoup(rawBody, source);
            log.info("RSS 크롤링 완료 (Jsoup 폴백): {} → {}건", source.getName(), articles.size());
            return articles;
        } catch (Exception jsoupEx) {
            log.error("RSS 크롤링 실패 [{}]: {}", source.getName(), jsoupEx.getMessage());
            return List.of();
        }
    }

    // ── Rome 파싱용 Article 변환 ──────────────────────────────────────────────

    private List<Article> toArticles(List<SyndEntry> entries, Source source) {
        List<Article> articles = new ArrayList<>();
        for (SyndEntry entry : entries) {
            Article article = new Article();
            article.setUrl(entry.getLink());
            article.setTitle(entry.getTitle());
            article.setSourceName(source.getName());
            article.setSourceType(source.getType());
            article.setCompetitor(source.getCompetitor());

            Date publishedDate = entry.getPublishedDate() != null
                    ? entry.getPublishedDate() : entry.getUpdatedDate();
            article.setPublishedAt(publishedDate != null
                    ? publishedDate.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
                    : LocalDateTime.now());

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
        return articles;
    }

    // ── Jsoup 폴백 파싱 (RSS 2.0 <item> / Atom <entry>) ─────────────────────

    private List<Article> parseWithJsoup(String body, Source source) {
        // Jsoup XML 파서: HTML void 태그를 닫지 않아도 파싱 가능
        Document doc = Jsoup.parse(body, "", org.jsoup.parser.Parser.xmlParser());

        // RSS 2.0 <item> 또는 Atom <entry>
        Elements items = doc.select("item");
        if (items.isEmpty()) items = doc.select("entry");

        List<Article> articles = new ArrayList<>();
        for (Element item : items) {
            String url = selectText(item, "link", "guid");
            String title = selectText(item, "title");
            if (url == null || url.isBlank() || title == null || title.isBlank()) continue;

            Article article = new Article();
            article.setUrl(url.trim());
            article.setTitle(title.trim());
            article.setSourceName(source.getName());
            article.setSourceType(source.getType());
            article.setCompetitor(source.getCompetitor());

            // 발행일 파싱 (RSS pubDate / Atom published|updated)
            String dateStr = selectText(item, "pubDate", "published", "updated");
            article.setPublishedAt(parseDateSafely(dateStr));

            // 본문
            String rawContent = selectText(item, "description", "content", "summary");
            String plainText = rawContent != null ? Jsoup.parse(rawContent).text() : "";
            article.setOriginalContent(plainText.length() > 5000
                    ? plainText.substring(0, 5000) : plainText);

            articles.add(article);
        }
        return articles;
    }

    /** 여러 셀렉터 중 첫 번째로 값이 있는 것을 반환 */
    private String selectText(Element parent, String... selectors) {
        for (String sel : selectors) {
            Element el = parent.selectFirst(sel);
            if (el != null && !el.text().isBlank()) return el.text();
        }
        return null;
    }

    // ── 날짜 파싱 ────────────────────────────────────────────────────────────

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME,                                    // RSS pubDate
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH), // Atom
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    );

    private LocalDateTime parseDateSafely(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return java.time.ZonedDateTime.parse(dateStr.trim(), fmt)
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .toLocalDateTime();
            } catch (DateTimeParseException ignored) {}
        }
        log.debug("날짜 파싱 실패, 현재 시각 사용: {}", dateStr);
        return LocalDateTime.now();
    }

    // ── HTTP 수신 ─────────────────────────────────────────────────────────────

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

    // ── XML 1차 정제 (Rome 파싱 전처리) ──────────────────────────────────────

    /**
     * 자주 나타나는 비표준 패턴을 제거/변환해 Rome XML 파서가 수용 가능한 형태로 만든다.
     * 완전히 malformed된 피드는 Jsoup 폴백에서 처리.
     */
    private String sanitizeXml(String xml) {
        // 1) DOCTYPE 제거 (JAXP XXE 방지 설정과 충돌)
        xml = xml.replaceAll("(?si)<!DOCTYPE[^>]*(?:\\[[^]]*])?\\s*>", "");
        // 2) <script> 블록 제거
        xml = xml.replaceAll("(?si)<script[^>]*>.*?</script>", "");
        // 3) HTML5 불리언 속성 → XML 호환 형태 변환 (예: async → async="async")
        xml = xml.replaceAll(
                "(?i)\\s(async|defer|checked|disabled|selected|multiple|readonly|required|autofocus|autoplay|controls|loop|muted|open|reversed)(?=[\\s/>])",
                " $1=\"$1\""
        );
        // 4) HTML void 요소 → 자기닫기 XML 형태 변환
        //    예: <br> → <br/>, <img src="..."> → <img src="..."/>
        xml = xml.replaceAll(
                "(?i)<(br|hr|img|input|meta|col|area|base|embed|param|source|track|wbr)(\\s[^>]*)?>",
                "<$1$2/>"
        );
        // 5) HTML <link> 변환 — 속성이 있는 경우만 자기닫기 처리
        //    RSS <link>https://...</link>는 속성이 없으므로 이 패턴에 해당 없음
        xml = xml.replaceAll("(?i)<link(\\s[^>]+)>", "<link$1/>");
        return xml;
    }
}
