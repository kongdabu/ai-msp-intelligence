-- 초기 뉴스 소스 데이터 (H2 MERGE INTO - 중복 방지)
-- 전용 크롤러(LG CNS, SK AX, 베스핀, PwC, AI MSP 업계)는 코드에서 직접 Google News RSS를 사용하므로 여기 미등록

-- 일반 IT 뉴스 RSS
MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('CIO Korea', 'https://www.ciokorea.com/rss', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('VentureBeat AI', 'https://feeds.feedburner.com/venturebeat/SZYF', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('Google News - AI Agent', 'https://news.google.com/rss/search?q=%22AI+Agent%22+OR+%22AI+에이전트%22&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('Google News - 클라우드 MSP', 'https://news.google.com/rss/search?q=%22클라우드+MSP%22+OR+%22클라우드+관리%22+AI&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('Google News - 금융 AI', 'https://news.google.com/rss/search?q=%22금융+AI%22+OR+%22공공+AI%22+OR+%22AI+ITO%22&hl=ko&gl=KR&ceid=KR:ko', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('ZDNet Korea', 'https://zdnet.co.kr/rss/', 'NEWS', 'GENERAL', true, NULL, 0, 0);

MERGE INTO source (name, url, type, competitor, active, last_crawled_at, crawl_count, error_count)
KEY(url) VALUES
('전자신문', 'https://www.etnews.com/rss', 'NEWS', 'GENERAL', true, NULL, 0, 0);
