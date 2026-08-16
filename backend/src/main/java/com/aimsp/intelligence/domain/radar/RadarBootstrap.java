package com.aimsp.intelligence.domain.radar;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RadarBootstrap {

    private final RadarPlayerRepository radarPlayerRepository;
    private final RadarSignalRepository radarSignalRepository;

    @PostConstruct
    @Transactional
    public void initialize() {
        LocalDateTime now = LocalDateTime.now();
        radarPlayerRepository.saveAll(RadarCatalog.WATCHLIST.stream()
                .filter(seed -> !radarPlayerRepository.existsByName(seed.name()))
                .map(seed -> {
            RadarPlayer player = new RadarPlayer();
            player.setName(seed.name());
            player.setLayer(seed.layer());
            player.setCountry(seed.country());
            player.setWebsite(seed.website());
            player.setWatchPriority(seed.watchPriority());
            player.setActive(true);
            player.setCreatedAt(now);
            return player;
        }).toList());

        initializeSeedSignals(now);
    }

    private void initializeSeedSignals(LocalDateTime now) {
        java.util.Map<String, RadarPlayer> playersByName = radarPlayerRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(RadarPlayer::getName, java.util.function.Function.identity()));

        for (RadarCatalog.SignalSeed seed : RadarCatalog.SIGNAL_SEEDS) {
            if (radarSignalRepository.findBySourceUrl(seed.sourceUrl()).isPresent()) {
                continue;
            }
            java.util.LinkedHashSet<RadarPlayer> players = seed.playerNames().stream()
                    .map(playersByName::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            if (players.isEmpty()) continue;

            RadarSignal signal = new RadarSignal();
            signal.setTitle(seed.title());
            signal.setFact(seed.fact());
            signal.setSourceUrl(seed.sourceUrl());
            signal.setSourceTier(seed.sourceTier());
            signal.setSignalType(seed.signalType());
            signal.setOccurredAt(now.minusDays(2));
            signal.setCapturedAt(now);
            signal.setConfidenceScore(seed.confidenceScore());
            signal.setImpactScore(seed.impactScore());
            signal.setLenses(new java.util.LinkedHashSet<>(seed.lenses()));
            signal.setPlayers(players);
            signal.setSourceVerifiedAt(now);

            RadarAssessment assessment = new RadarAssessment();
            assessment.setSignal(signal);
            assessment.setWhatChanged(seed.whatChanged());
            assessment.setIndustryStructureImpact(seed.industryStructureImpact());
            assessment.setMspOpportunity(seed.mspOpportunity());
            assessment.setMspThreat(seed.mspThreat());
            assessment.setStructuralRisk(seed.structuralRisk());
            assessment.setRecommendedAction(seed.recommendedAction());
            assessment.setDeliveryModel(seed.deliveryModel());
            assessment.setPricingModel(seed.pricingModel());
            assessment.setGeneratedAt(now);
            signal.setAssessment(assessment);

            radarSignalRepository.save(signal);
        }
    }
}
