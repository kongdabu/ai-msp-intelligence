package com.aimsp.intelligence.crawler;

import com.aimsp.intelligence.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcurementApiClient {

    private static final String BASE_URL = "https://apis.data.go.kr/1230000/ad/BidPublicInfoService";
    private static final String DETAIL_BASE_URL = "https://www.g2b.go.kr:8101/ep/tbid/tbBidList.do";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // 조회 대상 operation (용역·기타·물품 순)
    private static final List<String> OPERATIONS = List.of(
            "getBidPblancListInfoServc",  // 용역 (AI, 클라우드, MSP 주력)
            "getBidPblancListInfoEtc",    // 기타
            "getBidPblancListInfoThng"    // 물품
    );

    private final AppConfig appConfig;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public List<ProcurementItem> search(String keyword, int numOfRows) {
        if (appConfig.getProcurementApiKey().isBlank()) {
            log.warn("[나라장터] API 키 미설정 — 수집 스킵: {}", keyword);
            return List.of();
        }

        List<ProcurementItem> results = new ArrayList<>();
        for (String operation : OPERATIONS) {
            results.addAll(searchByOperation(operation, keyword, numOfRows));
        }
        return results;
    }

    private List<ProcurementItem> searchByOperation(String operation, String keyword, int numOfRows) {
        String apiUrl = BASE_URL + "/" + operation;

        HttpUrl url = HttpUrl.parse(apiUrl).newBuilder()
                .addQueryParameter("serviceKey", appConfig.getProcurementApiKey())
                .addQueryParameter("pageNo", "1")
                .addQueryParameter("numOfRows", String.valueOf(numOfRows))
                .addQueryParameter("inqryDiv", "1")   // 1: 공고명 검색
                .addQueryParameter("bidNm", keyword)   // BidPublicInfoService는 bidNm 파라미터 사용
                .addQueryParameter("type", "xml")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (compatible)")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.warn("[나라장터] {} HTTP {} [키워드: {}] 응답: {}",
                        operation, response.code(), keyword,
                        body.length() > 300 ? body.substring(0, 300) : body);
                return List.of();
            }
            List<ProcurementItem> items = parseXml(body);
            if (!items.isEmpty()) {
                log.info("[나라장터] {} '{}' 조회 {}건", operation, keyword, items.size());
            }
            return items;
        } catch (Exception e) {
            log.error("[나라장터] {} 호출 실패 [키워드: {}]: {}", operation, keyword, e.getMessage());
            return List.of();
        }
    }

    private List<ProcurementItem> parseXml(String xml) {
        List<ProcurementItem> items = new ArrayList<>();
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        for (Element item : doc.select("item")) {
            String bidNtceNm    = text(item, "bidNtceNm");
            String bidNtceNo    = text(item, "bidNtceNo");
            String bidNtceSqNo  = text(item, "bidNtceSqNo");
            String ntceInsttNm  = text(item, "ntceInsttNm");
            String bidNtceDt    = text(item, "bidNtceDt");
            String asignBdgtAmt = text(item, "asignBdgtAmt");

            if (bidNtceNm.isBlank()) continue;

            items.add(new ProcurementItem(
                    bidNtceNm,
                    buildDetailUrl(bidNtceNo, bidNtceSqNo),
                    ntceInsttNm,
                    bidNtceDt,
                    asignBdgtAmt,
                    parseDate(bidNtceDt)
            ));
        }
        return items;
    }

    private String text(Element el, String tag) {
        Elements found = el.select(tag);
        return found.isEmpty() ? "" : found.first().text().trim();
    }

    private String buildDetailUrl(String bidNtceNo, String bidNtceSqNo) {
        if (bidNtceNo.isBlank()) return DETAIL_BASE_URL;
        return DETAIL_BASE_URL + "?bidno=" + bidNtceNo + "&bidseq=" + bidNtceSqNo;
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(dateStr.trim(), DATE_FMT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public record ProcurementItem(
            String title,
            String url,
            String institutionName,
            String bidDate,
            String budgetAmount,
            LocalDateTime publishedAt
    ) {}
}
