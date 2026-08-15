package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.domain.article.Article;

import java.util.List;

/** 외부 콘텐츠 소스에서 기사 후보를 수집하는 공통 계약이다. */
public interface ContentSourceCrawler {

    List<Article> crawl();
}
