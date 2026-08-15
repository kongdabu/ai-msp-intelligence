package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.config.AppConfig;
import com.aimsp.intelligence.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 신뢰할 수 있는 공식 사이트의 목록과 상세 페이지를 수집한다.
 * RSS·공식 API가 제공되는 소스는 향후 같은 계약을 구현해 우선 적용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialSiteCrawler implements ContentSourceCrawler {

    private static final String USER_AGENT = "AI-MSP-Intelligence/1.0 (+https://ai-msp-intelligence.vercel.app)";
    private static final int REQUEST_TIMEOUT_MS = 15_000;
    private static final int MAX_CONTENT_LENGTH = 5_000;
    private static final int MIN_CONTENT_LENGTH = 300;
    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final Set<String> TOPIC_KEYWORDS = Set.of(
            "ai", "artificial intelligence", "generative", "agent", "llm", "copilot",
            "ai pricing", "ai monetization", "ai workforce", "ai talent", "ai training", "ai upskilling", "ai reskilling",
            "인공지능", "생성형", "에이전트", "파운데이션 모델", "aiops",
            "ai 가격", "ai 과금", "ai 인력", "ai 인재", "ai 교육", "리스킬링",
            "生成ai", "人工知能", "エージェント", "基盤モデル", "大規模言語モデル", "ai 価格モデル", "ai 人材育成", "ai リスキリング"
    );

    private final AppConfig appConfig;
    private final ConcurrentMap<String, RobotsPolicy> robotsPolicies = new ConcurrentHashMap<>();

    private static final List<SiteDefinition> SITE_DEFINITIONS = List.of(
            new SiteDefinition(
                    "Bain & Company Insights",
                    "https://www.bain.com/insights/",
                    "www.bain.com",
                    "/insights/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "Anthropic News",
                    "https://www.anthropic.com/news",
                    "www.anthropic.com",
                    "/news/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "BCG Publications",
                    "https://www.bcg.com/publications",
                    "www.bcg.com",
                    "/publications/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "Accenture Insights",
                    "https://www.accenture.com/us-en/insights",
                    "www.accenture.com",
                    "/us-en/insights/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "OpenAI News",
                    "https://openai.com/news/",
                    "openai.com",
                    "/index/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "Google DeepMind Blog",
                    "https://deepmind.google/discover/blog/",
                    "deepmind.google",
                    "/discover/blog/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "NVIDIA AI Blog",
                    "https://blogs.nvidia.com/blog/category/ai/",
                    "blogs.nvidia.com",
                    "/blog/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "LG CNS Newsroom",
                    "https://www.lgcns.com/kr/newsroom/press",
                    "www.lgcns.com",
                    "/kr/newsroom/press/",
                    "LG_CNS"
            ),
            new SiteDefinition(
                    "NTT DATA Japan",
                    "https://www.nttdata.com/jp/ja/news/",
                    "www.nttdata.com",
                    "/jp/ja/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "NEC Press Releases",
                    "https://jpn.nec.com/press/",
                    "jpn.nec.com",
                    "/press/",
                    "GENERAL"
            ),
            new SiteDefinition(
                    "Hitachi News Releases",
                    "https://www.hitachi.com/en/press/",
                    "www.hitachi.com",
                    "/en/press/",
                    "GENERAL"
            )
    );

    @Override
    public List<Article> crawl() {
        if (!appConfig.isOfficialSiteCrawlEnabled()) {
            log.info("공식 사이트 수집이 비활성화되어 있습니다.");
            return List.of();
        }

        List<Article> articles = new ArrayList<>();
        for (SiteDefinition site : SITE_DEFINITIONS) {
            articles.addAll(crawlSite(site));
        }
        return articles;
    }

    private List<Article> crawlSite(SiteDefinition site) {
        try {
            if (!isAllowedByRobots(site.listUrl())) {
                log.warn("robots.txt 정책으로 공식 사이트 수집 제외: {}", site.sourceName());
                return List.of();
            }

            Document listDocument = fetch(site.listUrl());
            List<String> articleUrls = findArticleUrls(listDocument, site);
            List<Article> articles = new ArrayList<>();
            for (String articleUrl : articleUrls) {
                fetchDelay();
                parseArticle(articleUrl, site).ifPresent(articles::add);
            }
            log.info("[{}] 공식 사이트 기사 후보 {}건 수집", site.sourceName(), articles.size());
            return articles;
        } catch (Exception e) {
            log.error("공식 사이트 수집 실패 [{}]: {}", site.sourceName(), e.getMessage());
            return List.of();
        }
    }

    private List<String> findArticleUrls(Document document, SiteDefinition site) {
        return document.select("a[href]").stream()
                .filter(element -> isTopicRelevant(element.text(), element.attr("href")))
                .map(element -> element.absUrl("href"))
                .map(this::canonicalizeUrl)
                .flatMap(Optional::stream)
                .filter(url -> isArticleUrl(url, site))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isArticleUrl(String url, SiteDefinition site) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            return site.allowedHost().equalsIgnoreCase(uri.getHost())
                    && path != null
                    && path.startsWith(site.articlePathPrefix())
                    && !normalizePath(path).equals(normalizePath(new URI(site.listUrl()).getPath()));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private Optional<Article> parseArticle(String articleUrl, SiteDefinition site) {
        try {
            if (!isAllowedByRobots(articleUrl)) {
                log.debug("robots.txt 정책으로 상세 페이지 제외: {}", articleUrl);
                return Optional.empty();
            }

            Document document = fetch(articleUrl);
            String title = document.select("meta[property=og:title]").attr("content");
            if (title.isBlank()) {
                title = document.title();
            }
            String content = extractContent(document);
            if (content.length() < MIN_CONTENT_LENGTH || !isTopicRelevant(title, content)) {
                return Optional.empty();
            }
            Optional<LocalDateTime> publishedAt = extractPublishedAt(document);
            if (publishedAt.isEmpty() || publishedAt.get().isBefore(LocalDateTime.now().minusDays(appConfig.getOfficialSiteMaxAgeDays()))) {
                return Optional.empty();
            }

            Article article = new Article();
            article.setUrl(articleUrl);
            article.setTitle(truncate(title, 500));
            article.setOriginalContent(content);
            article.setSourceName(site.sourceName());
            article.setSourceType("HOMEPAGE");
            article.setCompetitor(site.competitor());
            article.setPublishedAt(publishedAt.get());
            return Optional.of(article);
        } catch (Exception e) {
            log.warn("공식 사이트 상세 페이지 처리 실패 [{}]: {}", articleUrl, e.getMessage());
            return Optional.empty();
        }
    }

    private Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer("https://www.google.com/")
                .timeout(REQUEST_TIMEOUT_MS)
                .followRedirects(true)
                .get();
    }

    private String extractContent(Document document) {
        Element mainContent = document.selectFirst("article");
        if (mainContent == null) {
            mainContent = document.selectFirst("main");
        }
        if (mainContent == null) {
            mainContent = document.body();
        }
        String content = mainContent.select("p, li").stream()
                .map(Element::text)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining("\n"));
        return truncate(content, MAX_CONTENT_LENGTH);
    }

    private Optional<LocalDateTime> extractPublishedAt(Document document) {
        List<String> dateValues = List.of(
                document.select("meta[property=article:published_time]").attr("content"),
                document.select("meta[name=date]").attr("content"),
                document.select("time[datetime]").attr("datetime")
        );
        return dateValues.stream()
                .filter(value -> !value.isBlank())
                .map(this::parseDate)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<LocalDateTime> parseDate(String value) {
        try {
            return Optional.of(OffsetDateTime.parse(value).atZoneSameInstant(SEOUL_ZONE_ID).toLocalDateTime());
        } catch (Exception ignored) {
            try {
                return Optional.of(ZonedDateTime.parse(value).withZoneSameInstant(SEOUL_ZONE_ID).toLocalDateTime());
            } catch (Exception ignoredAgain) {
                try {
                    return Optional.of(LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception ignoredFinal) {
                    try {
                        return Optional.of(LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
                    } catch (Exception ignoredDateOnly) {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    private boolean isTopicRelevant(String title, String content) {
        String text = (title + " " + content).toLowerCase(Locale.ROOT);
        return TOPIC_KEYWORDS.stream().anyMatch(keyword -> containsTopicKeyword(text, keyword));
    }

    private boolean containsTopicKeyword(String text, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        if ("ai".equals(normalizedKeyword) || "llm".equals(normalizedKeyword)) {
            return text.matches("(?s).*(?<![a-z])" + normalizedKeyword + "(?![a-z]).*");
        }
        return text.contains(normalizedKeyword);
    }

    private boolean isAllowedByRobots(String pageUrl) {
        try {
            URI uri = new URI(pageUrl);
            String origin = uri.getScheme() + "://" + uri.getHost();
            RobotsPolicy policy = robotsPolicies.computeIfAbsent(origin, this::loadRobotsPolicy);
            return policy.isAllowed(uri.getPath());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private RobotsPolicy loadRobotsPolicy(String origin) {
        try {
            Document robotsDocument = Jsoup.connect(origin + "/robots.txt")
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT_MS)
                    .ignoreContentType(true)
                    .get();
            return RobotsPolicy.from(robotsDocument.wholeText());
        } catch (Exception e) {
            log.warn("robots.txt를 확인할 수 없어 해당 사이트 수집을 건너뜁니다: {}", origin);
            return RobotsPolicy.disallowAll();
        }
    }

    private void fetchDelay() {
        try {
            Thread.sleep(appConfig.getOfficialSiteRequestDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Optional<String> canonicalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            return Optional.of(new URI(
                    uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null
            ).toString());
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    private String normalizePath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record SiteDefinition(
            String sourceName,
            String listUrl,
            String allowedHost,
            String articlePathPrefix,
            String competitor
    ) {
    }

    private record RobotsPolicy(List<RobotsRule> rules) {

        static RobotsPolicy from(String content) {
            List<RobotsRule> rules = new ArrayList<>();
            boolean appliesToAllAgents = false;
            boolean groupHasRules = false;
            for (String line : content.lines().map(String::trim).toList()) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length != 2) {
                    continue;
                }
                String directive = parts[0].trim().toLowerCase(Locale.ROOT);
                String value = parts[1].trim();
                if ("user-agent".equals(directive)) {
                    if (groupHasRules) {
                        appliesToAllAgents = false;
                        groupHasRules = false;
                    }
                    appliesToAllAgents = appliesToAllAgents || "*".equals(value);
                } else if ("allow".equals(directive) || "disallow".equals(directive)) {
                    groupHasRules = true;
                    if (appliesToAllAgents && !value.isBlank()) {
                        rules.add(new RobotsRule(value, "allow".equals(directive)));
                    }
                }
            }
            return new RobotsPolicy(rules);
        }

        static RobotsPolicy disallowAll() {
            return new RobotsPolicy(List.of(new RobotsRule("/", false)));
        }

        boolean isAllowed(String path) {
            return rules.stream()
                    .filter(rule -> rule.matches(path))
                    .max((first, second) -> {
                        int specificityComparison = Integer.compare(first.specificity(), second.specificity());
                        return specificityComparison != 0
                                ? specificityComparison
                                : Boolean.compare(first.allowed(), second.allowed());
                    })
                    .map(RobotsRule::allowed)
                    .orElse(true);
        }
    }

    private record RobotsRule(String pathPattern, boolean allowed) {

        boolean matches(String path) {
            boolean endAnchored = pathPattern.endsWith("$");
            String basePattern = endAnchored ? pathPattern.substring(0, pathPattern.length() - 1) : pathPattern;
            String regex = "^" + Pattern.quote(basePattern).replace("*", "\\E.*\\Q")
                    + (endAnchored ? "$" : ".*");
            return path.matches(regex);
        }

        int specificity() {
            return pathPattern.replace("*", "").length();
        }
    }
}
