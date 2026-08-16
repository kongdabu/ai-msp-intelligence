package com.aimsp.intelligence.domain.radar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadarBootstrapTest {

    @Mock
    private RadarPlayerRepository radarPlayerRepository;

    @Mock
    private RadarSignalRepository radarSignalRepository;

    @InjectMocks
    private RadarBootstrap radarBootstrap;

    @Test
    @DisplayName("RadarBootstrap 실행 시 Watchlist 및 Seed Signal이 정상 등록된다")
    void initialize_success() {
        // given
        RadarPlayer openai = new RadarPlayer();
        openai.setName("OpenAI");
        openai.setLayer("FRONTIER_LAB");

        RadarPlayer anthropic = new RadarPlayer();
        anthropic.setName("Anthropic");
        anthropic.setLayer("FRONTIER_LAB");

        given(radarPlayerRepository.findAll()).willReturn(List.of(openai, anthropic));
        given(radarSignalRepository.findBySourceUrl(any())).willReturn(Optional.empty());

        // when
        radarBootstrap.initialize();

        // then
        ArgumentCaptor<RadarSignal> signalCaptor = ArgumentCaptor.forClass(RadarSignal.class);
        verify(radarSignalRepository, atLeastOnce()).save(signalCaptor.capture());

        List<RadarSignal> savedSignals = signalCaptor.getAllValues();
        assertThat(savedSignals).isNotEmpty();
        assertThat(savedSignals).anyMatch(signal -> signal.getLenses().contains("AI_PRICING"));
    }
}
