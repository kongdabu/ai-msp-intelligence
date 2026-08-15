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

    @PostConstruct
    @Transactional
    public void initializeWatchlist() {
        if (radarPlayerRepository.count() > 0) return;

        LocalDateTime now = LocalDateTime.now();
        radarPlayerRepository.saveAll(RadarCatalog.WATCHLIST.stream().map(seed -> {
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
    }
}
