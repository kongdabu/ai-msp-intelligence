package com.aimsp.intelligence.domain.radar;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "radar_signal")
@Getter
@Setter
@NoArgsConstructor
public class RadarSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fact;

    @Column(nullable = false, unique = true, length = 2000)
    private String sourceUrl;

    @Column(nullable = false, length = 20)
    private String sourceTier;

    @Column(nullable = false, length = 40)
    private String signalType;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    @Column(nullable = false)
    private int confidenceScore;

    @Column(nullable = false)
    private int impactScore;

    @Column(nullable = false, length = 20)
    private String status = "NEW";

    private LocalDateTime sourceVerifiedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "radar_signal_lens", joinColumns = @JoinColumn(name = "signal_id"))
    @Column(name = "lens", length = 40)
    private Set<String> lenses = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "radar_signal_player",
            joinColumns = @JoinColumn(name = "signal_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id"))
    private Set<RadarPlayer> players = new LinkedHashSet<>();

    @OneToOne(mappedBy = "signal", cascade = CascadeType.ALL, orphanRemoval = true)
    private RadarAssessment assessment;
}
