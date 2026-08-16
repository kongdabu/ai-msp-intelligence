package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.dto.RadarDto;
import com.aimsp.intelligence.dto.PageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
public class RadarService {

    private final RadarPlayerRepository radarPlayerRepository;
    private final RadarSignalRepository radarSignalRepository;
    private final RadarWeeklyBriefRepository radarWeeklyBriefRepository;
    private final RadarSourceVerifier radarSourceVerifier;

    @Transactional(readOnly = true)
    public RadarDto.OverviewResponse getOverview() {
        Map<String, Long> signalCountByLens = radarSignalRepository
                .countByLensExcludingStatus(RadarSourceVerificationService.SOURCE_UNAVAILABLE).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        List<RadarSignal> recentSignals = radarSignalRepository
                .findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(RadarSourceVerificationService.SOURCE_UNAVAILABLE);
        List<RadarDto.LensResponse> lenses = RadarCatalog.LENSES.stream()
                .map(lens -> new RadarDto.LensResponse(lens.code(), lens.label(), lens.description(),
                        signalCountByLens.getOrDefault(lens.code(), 0L)))
                .toList();
        long highImpactSignalCount = radarSignalRepository
                .countByStatusNotAndImpactScoreGreaterThanEqual(RadarSourceVerificationService.SOURCE_UNAVAILABLE, 80);

        return new RadarDto.OverviewResponse(
                Math.toIntExact(radarPlayerRepository.countByActiveTrue()),
                Math.toIntExact(radarSignalRepository.count()),
                Math.toIntExact(highImpactSignalCount),
                lenses,
                radarPlayerRepository.findByActiveTrueOrderByLayerAscWatchPriorityAscNameAsc().stream()
                        .map(RadarDto.PlayerResponse::from).toList(),
                recentSignals.stream().map(RadarDto.SignalResponse::from).toList(),
                radarWeeklyBriefRepository.findTop8ByOrderByPeriodEndDesc().stream()
                        .map(RadarDto.WeeklyBriefResponse::from).toList()
        );
    }

    @Transactional(readOnly = true)
    public PageResponseDto<RadarDto.SignalResponse> getSignals(String lens, Integer minimumImpactScore, int page, int size) {
        if (lens != null && !lens.isBlank() && RadarCatalog.LENSES.stream().noneMatch(item -> item.code().equals(lens))) {
            throw new IllegalArgumentException("지원하지 않는 Radar 관점입니다: " + lens);
        }
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Specification<RadarSignal> specification = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), RadarSourceVerificationService.SOURCE_UNAVAILABLE));
            if (lens != null && !lens.isBlank()) {
                predicates.add(cb.equal(root.join("lenses"), lens));
                query.distinct(true);
            }
            if (minimumImpactScore != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("impactScore"), Math.min(Math.max(minimumImpactScore, 0), 100)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Order.desc("impactScore"), Sort.Order.desc("occurredAt"), Sort.Order.desc("capturedAt")));
        return PageResponseDto.from(radarSignalRepository.findAll(specification, pageable).map(RadarDto.SignalResponse::from));
    }

    @Transactional(readOnly = true)
    public List<RadarDto.PlayerResponse> getPlayers() {
        return radarPlayerRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(RadarPlayer::getLayer)
                        .thenComparingInt(RadarPlayer::getWatchPriority)
                        .thenComparing(RadarPlayer::getName))
                .map(RadarDto.PlayerResponse::from)
                .toList();
    }

    @Transactional
    public RadarDto.PlayerResponse updatePlayer(Long id, RadarDto.PlayerUpdateRequest request) {
        RadarPlayer player = radarPlayerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Watch List 항목을 찾을 수 없습니다: " + id));
        player.setWebsite(request.website());
        player.setWatchPriority(request.watchPriority());
        player.setActive(request.active());
        return RadarDto.PlayerResponse.from(radarPlayerRepository.save(player));
    }

    @Transactional
    public RadarDto.SignalResponse registerSignal(RadarDto.SignalRequest request) {
        if (radarSignalRepository.findBySourceUrl(request.sourceUrl()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 출처 URL입니다.");
        }
        validateLenses(request.lenses());
        RadarSourceVerifier.SourceCheckResult sourceCheck = radarSourceVerifier.check(request.sourceUrl());
        if (sourceCheck.status() == RadarSourceVerifier.CheckStatus.REJECTED) {
            throw new IllegalArgumentException("공개 HTTP/HTTPS 원문 URL만 등록할 수 있습니다.");
        }
        if (sourceCheck.status() == RadarSourceVerifier.CheckStatus.UNAVAILABLE) {
            throw new IllegalArgumentException("원문 URL을 확인할 수 없어 Radar Signal로 등록하지 않았습니다.");
        }

        Map<String, RadarPlayer> playersByName = radarPlayerRepository.findAll().stream()
                .collect(Collectors.toMap(RadarPlayer::getName, Function.identity()));
        LinkedHashSet<RadarPlayer> players = request.playerNames().stream()
                .map(name -> {
                    RadarPlayer player = playersByName.get(name);
                    if (player == null) throw new IllegalArgumentException("Watch List에 없는 플레이어입니다: " + name);
                    return player;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LocalDateTime now = LocalDateTime.now();
        RadarSignal signal = new RadarSignal();
        signal.setTitle(request.title());
        signal.setFact(request.fact());
        signal.setSourceUrl(request.sourceUrl());
        signal.setSourceTier(request.sourceTier());
        signal.setSignalType(request.signalType());
        signal.setOccurredAt(request.occurredAt());
        signal.setCapturedAt(now);
        signal.setConfidenceScore(request.confidenceScore());
        signal.setImpactScore(request.impactScore());
        signal.setLenses(new LinkedHashSet<>(request.lenses()));
        signal.setPlayers(players);
        if (sourceCheck.status() == RadarSourceVerifier.CheckStatus.AVAILABLE) {
            signal.setSourceVerifiedAt(now);
        }

        RadarAssessment assessment = new RadarAssessment();
        assessment.setSignal(signal);
        assessment.setWhatChanged(request.assessment().whatChanged());
        assessment.setIndustryStructureImpact(request.assessment().industryStructureImpact());
        assessment.setMspOpportunity(request.assessment().mspOpportunity());
        assessment.setMspThreat(request.assessment().mspThreat());
        assessment.setStructuralRisk(request.assessment().structuralRisk());
        assessment.setRecommendedAction(request.assessment().recommendedAction());
        assessment.setDeliveryModel(request.assessment().deliveryModel());
        assessment.setPricingModel(request.assessment().pricingModel());
        assessment.setGeneratedAt(now);
        signal.setAssessment(assessment);

        return RadarDto.SignalResponse.from(radarSignalRepository.save(signal));
    }

    @Transactional
    public RadarDto.WeeklyBriefResponse generateWeeklyBrief() {
        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(7);
        List<RadarSignal> signals = radarSignalRepository
                .findByOccurredAtBetweenOrderByImpactScoreDescOccurredAtDesc(periodStart, periodEnd).stream()
                .filter(signal -> !RadarSourceVerificationService.SOURCE_UNAVAILABLE.equals(signal.getStatus()))
                .toList();
        if (signals.isEmpty()) {
            throw new IllegalArgumentException("최근 7일간 주간 브리핑을 만들 검증 신호가 없습니다.");
        }

        RadarWeeklyBrief brief = new RadarWeeklyBrief();
        brief.setPeriodStart(periodStart);
        brief.setPeriodEnd(periodEnd);
        brief.setTitle("AI Services Industry Radar 주간 브리핑");
        brief.setExecutiveSummary("최근 7일간 검증된 산업 신호 " + signals.size()
                + "건을 기반으로 플레이어 이동, 파트너십, 딜리버리·가격·운영 모델 변화를 정리했습니다.");
        brief.setPlayerMoves(summarizeSignals(signals, "FRONTIER_LABS", "AI_AGENT"));
        brief.setPartnershipChanges(summarizeSignals(signals, "PARTNERSHIP"));
        brief.setDeliveryModelChanges(summarizeSignals(signals, "DEPLOYMENT_MODEL"));
        brief.setPricingChanges(summarizeSignals(signals, "AI_PRICING"));
        brief.setAgenticOperationsChanges(summarizeSignals(signals, "AGENTIC_OPERATIONS"));
        brief.setKoreaImpact(signals.stream().map(RadarSignal::getAssessment)
                .filter(assessment -> assessment != null)
                .map(RadarAssessment::getRecommendedAction)
                .distinct()
                .collect(Collectors.joining("\n- ", "- ", "")));
        brief.setGeneratedAt(periodEnd);
        return RadarDto.WeeklyBriefResponse.from(radarWeeklyBriefRepository.save(brief));
    }

    private void validateLenses(Iterable<String> requestedLenses) {
        List<String> supportedLenses = RadarCatalog.LENSES.stream().map(RadarCatalog.Lens::code).toList();
        for (String lens : requestedLenses) {
            if (!supportedLenses.contains(lens)) {
                throw new IllegalArgumentException("지원하지 않는 Radar 관점입니다: " + lens);
            }
        }
    }

    private String summarizeSignals(List<RadarSignal> signals, String... targetLenses) {
        List<String> selected = signals.stream()
                .filter(signal -> signal.getLenses().stream().anyMatch(lens -> List.of(targetLenses).contains(lens)))
                .map(signal -> "- " + signal.getTitle() + " (" + signal.getPlayers().stream()
                        .map(RadarPlayer::getName).sorted().collect(Collectors.joining(", ")) + ")")
                .toList();
        return selected.isEmpty() ? "- 해당 기간에 확인된 핵심 신호가 없습니다." : String.join("\n", selected);
    }
}
