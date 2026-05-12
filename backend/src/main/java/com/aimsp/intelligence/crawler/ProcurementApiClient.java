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

    private static final String API_URL = "https://apis.data.go.kr/1230000/NaraInfoSrvc/getProcurementDetail";
    private static final String DETAIL_BASE_URL = "https://www.g2b.go.kr:8101/ep/tbid/tbBidList.do";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

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

        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("serviceKey", appConfig.getProcurementApiKey())
                .addQueryParameter("pageNo", "1")
                .addQueryParameter("numOfRows", String.valueOf(numOfRows))
                .addQueryParameter("inqryDiv", "1") // 1: 공고명 검색
                .addQueryParameter("searchNm", keyword)
                .addQueryParameter("type", "xml")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (compatible)")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("[나라장터] API HTTP {}: {}", response.code(), keyword);
                return List.of();
            }
            return parseXml(response.body().string());
        } catch (Exception e) {
            log.error("[나라장터] API 호출 실패 [{}]: {}", keyword, e.getMessage());
            return List.of();
        }
    }

    private List<ProcurementItem> parseXml(String xml) {
        List<ProcurementItem> items = new ArrayList<>();
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        for (Element item : doc.select("item")) {
            String bidNtceNm = text(item, "bidNtceNm");
            String bidNtceNo = text(item, "bidNtceNo");
            String bidNtceSqNo = text(item, "bidNtceSqNo");
            String ntceInsttNm = text(item, "ntceInsttNm");
            String bidNtceDt  = text(item, "bidNtceDt");
            String asignBdgtAmt = text(item, "asignBdgtAmt");

            if (bidNtceNm.isBlank()) continue;

            String detailUrl = buildDetailUrl(bidNtceNo, bidNtceSqNo);
            LocalDateTime publishedAt = parseDate(bidNtceDt);

            items.add(new ProcurementItem(bidNtceNm, detailUrl, ntceInsttNm, bidNtceDt, asignBdgtAmt, publishedAt));
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
