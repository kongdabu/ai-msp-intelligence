package com.aimsp.intelligence.crawler.sources;

import com.aimsp.intelligence.crawler.JobPostingApiClient;
import com.aimsp.intelligence.domain.article.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobPostingCrawler {

    private static final int ITEMS_PER_COMPANY = 15;

    // 검색 키워드 → Competitor 매핑
    private static final Map<String, String> COMPANY_COMPETITOR_MAP = Map.of(
            "LG CNS", "LG_CNS",
            "SK AX", "SK_AX",
            "베스핀글로벌", "BESPIN",
            "PwC", "PWC"
    );

    private final JobPostingApiClient jobPostingApiClient;

    public List<Article> crawl() {
        List<Article> articles = new ArrayList<>();

        for (Map.Entry<String, String> entry : COMPANY_COMPETITOR_MAP.entrySet()) {
            String companyName = entry.getKey();
            String competitor  = entry.getValue();

            List<JobPostingApiClient.JobItem> items =
                    jobPostingApiClient.searchByCompany(companyName, ITEMS_PER_COMPANY);

            for (JobPostingApiClient.JobItem item : items) {
                String detailUrl = item.url() != null ? item.url().detail() : "";
                if (detailUrl == null || detailUrl.isBlank()) continue;

                Article article = new Article();
                article.setTitle(buildTitle(companyName, item));
                article.setUrl(detailUrl);
                article.setOriginalContent(buildContent(item));
                article.setSourceName("사람인");
                article.setSourceType("JOB_POSTING");
                article.setCompetitor(competitor);
                article.setPublishedAt(item.postingDate());
                article.setCollectedAt(LocalDateTime.now());
                articles.add(article);
            }
        }

        log.info("[채용공고] 수집 완료: {}건", articles.size());
        return articles;
    }

    private String buildTitle(String companyName, JobPostingApiClient.JobItem item) {
        String posTitle = item.position() != null ? item.position().title() : "";
        return "[" + companyName + "] " + (posTitle != null ? posTitle : "채용공고");
    }

    private String buildContent(JobPostingApiClient.JobItem item) {
        if (item.position() == null) return "";
        String desc = item.position().description();
        return desc != null ? desc : "";
    }
}
