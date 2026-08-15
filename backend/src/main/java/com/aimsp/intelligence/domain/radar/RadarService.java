package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.dto.RadarDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RadarService {

    private final RadarPlayerRepository radarPlayerRepository;
    private final RadarSignalRepository radarSignalRepository;
    private final RadarWeeklyBriefRepository radarWeeklyBriefRepository;
    private final RadarSourceVerifier radarSourceVerifier;

    @Transactional(readOnly = true)
    public RadarDto.OverviewResponse getOverview() {
        List<RadarSignal> recentSignals = radarSignalRepository
                .findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(RadarSourceVerificationService.SOURCE_UNAVAILABLE);
        List<RadarDto.LensResponse> lenses = RadarCatalog.LENSES.stream()
                .map(lens -> new RadarDto.LensResponse(lens.code(), lens.label(), lens.description(),
                        recentSignals.stream().filter(signal -> signal.getLenses().contains(lens.code())).count()))
                .toList();
        long highImpactSignalCount = radarSignalRepository
                .countByStatusNotAndImpactScoreGreaterThanEqual(RadarSourceVerificationService.SOURCE_UNAVAILABLE, 80);

        return new RadarDto.OverviewResponse(
                Math.toIntExact(radarPlayerRepository.count()),
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
    public List<RadarDto.SignalResponse> getSignals() {
        return radarSignalRepository
                .findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(RadarSourceVerificationService.SOURCE_UNAVAILABLE).stream()
                .map(RadarDto.SignalResponse::from).toList();
    }

    @Transactional
    public RadarDto.SignalResponse registerSignal(RadarDto.SignalRequest request) {
        if (radarSignalRepository.findBySourceUrl(request.sourceUrl()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 출처 URL입니다.");
        }
        validateLenses(request.lenses());
        RadarSourceVerifier.SourceCheckResult sourceCheck = radarSourceVerifier.check(request.sourceUrl());
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
