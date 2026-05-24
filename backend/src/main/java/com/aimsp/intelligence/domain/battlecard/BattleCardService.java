package com.aimsp.intelligence.domain.battlecard;

import com.aimsp.intelligence.ai.BattleCardGenerator;
import com.aimsp.intelligence.ai.GeminiApiClient;
import com.aimsp.intelligence.domain.article.Article;
import com.aimsp.intelligence.domain.article.ArticleRepository;
import com.aimsp.intelligence.dto.BattleCardDto;
import com.aimsp.intelligence.exception.AiApiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BattleCardService {

    private static final List<String> COMPETITORS = List.of("LG_CNS", "SK_AX", "BESPIN", "PWC");
    private static final int ARTICLES_PER_COMPETITOR = 20;

    private final BattleCardRepository battleCardRepository;
    private final ArticleRepository articleRepository;
    private final BattleCardGenerator battleCardGenerator;
    private final GeminiApiClient geminiApiClient;
    private final PlatformTransactionManager transactionManager;

    // 경쟁사별 최신 배틀카드 1건씩 조회 (JOIN FETCH로 N+1 방지)
    @Transactional(readOnly = true)
    public List<BattleCardDto.Response> getLatestBattleCards() {
        List<BattleCardDto.Response> result = new ArrayList<>();
        for (String competitor : COMPETITORS) {
            battleCardRepository.findTopByCompetitorOrderByGeneratedAtDesc(competitor)
                    .map(BattleCardDto.Response::from)
                    .ifPresent(result::add);
        }
        return result;
    }

    // 특정 경쟁사 배틀카드 이력 조회 (최근 10건 제한)
    @Transactional(readOnly = true)
    public List<BattleCardDto.Response> getBattleCardsByCompetitor(String competitor) {
        return battleCardRepository.findTop10ByCompetitorOrderByGeneratedAtDesc(competitor)
                .stream()
                .map(BattleCardDto.Response::from)
                .collect(Collectors.toList());
    }

    // 배틀카드 단건 상세 조회 (출처 기사 포함)
    @Transactional(readOnly = true)
    public BattleCardDto.DetailResponse getBattleCardDetail(Long id) {
        BattleCard bc = battleCardRepository.findByIdWithArticles(id)
                .orElseThrow(() -> new IllegalArgumentException("배틀카드를 찾을 수 없습니다: " + id));
        return BattleCardDto.DetailResponse.from(bc);
    }

    // 전체 경쟁사 배틀카드 수동 생성
    public List<BattleCardDto.Response> generateBattleCards() {
        if (!geminiApiClient.isAvailable()) {
            throw new AiApiUnavailableException();
        }
        log.info("[배틀카드] 생성 시작 — {}개 경쟁사", COMPETITORS.size());

        List<BattleCardDto.Response> result = new ArrayList<>();
        for (String competitor : COMPETITORS) {
            try {
                // DB Read (Spring Data JPA findBy... is implicitly transactional/safe for read)
                List<Article> articles = articleRepository.findByCompetitorOrderByPublishedAtDesc(
                        competitor, PageRequest.of(0, ARTICLES_PER_COMPETITOR)).getContent();

                // Heavy sequential Gemini calls (run OUTSIDE database transaction)
                BattleCard bc = battleCardGenerator.generate(competitor, articles);
                if (bc == null) continue;

                // DB Write (wrapped in a short-lived transaction)
                BattleCardDto.Response savedDto = new TransactionTemplate(transactionManager).execute(status -> {
                    bc.getSourceArticles().forEach(bca -> bca.setBattleCard(bc));
                    return BattleCardDto.Response.from(battleCardRepository.save(bc));
                });

                if (savedDto != null) {
                    result.add(savedDto);
                }
            } catch (AiApiUnavailableException e) {
                // AI API 일시 오류는 해당 경쟁사만 스킵, 나머지 경쟁사 계속 진행
                log.warn("[배틀카드] AI API 오류로 스킵 [{}]: 다음 경쟁사로 계속", competitor);
            } catch (Exception e) {
                log.error("[배틀카드] 생성 실패 [{}]: {}", competitor, e.getMessage(), e);
            }
        }

        log.info("[배틀카드] 생성 완료: {}건", result.size());
        return result;
    }
}
