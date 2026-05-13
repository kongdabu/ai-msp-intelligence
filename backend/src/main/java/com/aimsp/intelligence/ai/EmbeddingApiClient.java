package com.aimsp.intelligence.ai;

import com.aimsp.intelligence.config.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingApiClient {

    public enum TaskType {
        RETRIEVAL_DOCUMENT,
        RETRIEVAL_QUERY
    }

    private static final String EMBEDDING_MODEL = "gemini-embedding-2";
    private static final int EMBEDDING_DIM = 3072;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 텍스트를 768차원 벡터로 임베딩.
     * API 키 미설정 또는 호출 실패 시 null 반환 (기사 저장 자체는 계속).
     */
    public float[] embed(String text, TaskType taskType) {
        if (appConfig.getGeminiApiKey().isBlank()) {
            return null;
        }
        if (text == null || text.isBlank()) {
            return null;
        }

        String truncated = text.length() > 2000 ? text.substring(0, 2000) : text;

        try {
            String url = appConfig.getGeminiApiUrl() + "/" + EMBEDDING_MODEL
                    + ":embedContent?key=" + appConfig.getGeminiApiKey();

            String body = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("model", "models/" + EMBEDDING_MODEL)
                            .put("taskType", taskType.name())
                            .set("content", objectMapper.createObjectNode()
                                    .set("parts", objectMapper.createArrayNode()
                                            .add(objectMapper.createObjectNode()
                                                    .put("text", truncated))))
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(body, JSON))
                    .header("content-type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 429) {
                    log.warn("[임베딩] Rate Limit(429) — 60초 대기 후 1회 재시도");
                    Thread.sleep(60000);
                    try (Response retry = client.newCall(request).execute()) {
                        if (!retry.isSuccessful() || retry.body() == null) {
                            log.warn("[임베딩] 재시도 실패 HTTP {}", retry.code());
                            return null;
                        }
                        return parseEmbedding(objectMapper.readTree(retry.body().string()));
                    }
                }
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("[임베딩] API 오류 HTTP {}", response.code());
                    return null;
                }
                return parseEmbedding(objectMapper.readTree(response.body().string()));
            }
        } catch (Exception e) {
            log.warn("[임베딩] 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    private float[] parseEmbedding(JsonNode root) {
        JsonNode values = root.path("embedding").path("values");
        if (values.isMissingNode() || values.size() != EMBEDDING_DIM) {
            log.warn("[임베딩] 응답 형식 오류: values.size={}", values.size());
            return null;
        }
        float[] embedding = new float[EMBEDDING_DIM];
        for (int i = 0; i < EMBEDDING_DIM; i++) {
            embedding[i] = (float) values.get(i).asDouble();
        }
        return embedding;
    }

    /**
     * float[] 임베딩을 pgvector 문자열 "[0.1,0.2,...]"으로 변환.
     * ArticleRepository.updateEmbedding() native SQL CAST에 사용.
     */
    public static String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        return sb.append(']').toString();
    }
}
