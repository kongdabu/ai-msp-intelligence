-- 초기 뉴스 소스 데이터 (PostgreSQL - INSERT ON CONFLICT)
INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('CIO Korea', 'https://www.ciokorea.com/rss', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('VentureBeat AI', 'https://feeds.feedburner.com/venturebeat/SZYF', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('Google News - AI Agent', 'https://news.google.com/rss/search?q=%22AI+Agent%22+OR+%22AI+에이전트%22&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('Google News - 클라우드 MSP', 'https://news.google.com/rss/search?q=%22클라우드+MSP%22+OR+%22클라우드+관리%22+AI&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('Google News - 금융 AI', 'https://news.google.com/rss/search?q=%22금융+AI%22+OR+%22공공+AI%22+OR+%22AI+ITO%22&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('ZDNet Korea', 'https://zdnet.co.kr/rss/', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;

INSERT INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
VALUES ('전자신문', 'https://www.etnews.com/rss', 'NEWS', 'GENERAL', true, NULL, 0, 0)
ON CONFLICT (url) DO NOTHING;
