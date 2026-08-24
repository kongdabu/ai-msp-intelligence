package com.aimsp.intelligence.domain.radar;

import com.aimsp.intelligence.dto.PageResponseDto;
import com.aimsp.intelligence.dto.RadarDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RadarServiceTest {

    @Mock
    private RadarPlayerRepository radarPlayerRepository;

    @Mock
    private RadarSignalRepository radarSignalRepository;

    @Mock
    private RadarWeeklyBriefRepository radarWeeklyBriefRepository;

    private RadarService radarService;

    @BeforeEach
    void setUp() {
        radarService = new RadarService(
                radarPlayerRepository,
                radarSignalRepository,
                radarWeeklyBriefRepository,
                new RadarSourceVerifier()
        );
    }

    @Test
    @DisplayName("신호 목록 조회 시 정렬 기준이 발생일자(occurredAt DESC) -> 영향도(impactScore DESC) -> 수집일자(capturedAt DESC) 순이어야 한다")
    void getSignals_shouldSortByOccurredAtThenImpactScoreThenCapturedAt() {
        // given
        RadarSignal signal = new RadarSignal();
        signal.setId(1L);
        signal.setTitle("테스트 신호");
        signal.setFact("테스트 팩트");
        signal.setSourceUrl("https://example.com/test");
        signal.setSourceTier("TIER_1");
        signal.setSignalType("TECH_BREAKTHROUGH");
        signal.setOccurredAt(LocalDateTime.of(2026, 8, 24, 10, 0));
        signal.setCapturedAt(LocalDateTime.of(2026, 8, 24, 11, 0));
        signal.setImpactScore(90);
        signal.setConfidenceScore(95);
        signal.setStatus("NEW");
        signal.setLenses(Set.of("AI_AGENT"));
        signal.setPlayers(Set.of());

        given(radarSignalRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(signal)));

        // when
        PageResponseDto<RadarDto.SignalResponse> result = radarService.getSignals(null, null, 0, 20);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("테스트 신호");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(radarSignalRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        Sort sort = pageable.getSort();
        assertThat(sort).isNotNull();

        List<Sort.Order> orders = sort.stream().toList();
        assertThat(orders).hasSize(3);

        // 1순위: occurredAt DESC (일자별)
        assertThat(orders.get(0).getProperty()).isEqualTo("occurredAt");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);

        // 2순위: impactScore DESC (영향도순)
        assertThat(orders.get(1).getProperty()).isEqualTo("impactScore");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);

        // 3순위: capturedAt DESC (수집일자순)
        assertThat(orders.get(2).getProperty()).isEqualTo("capturedAt");
        assertThat(orders.get(2).getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
