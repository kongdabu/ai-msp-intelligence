package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.radar.RadarAssessment;
import com.aimsp.intelligence.domain.radar.RadarPlayer;
import com.aimsp.intelligence.domain.radar.RadarSignal;
import com.aimsp.intelligence.domain.radar.RadarWeeklyBrief;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public final class RadarDto {

    private RadarDto() {
    }

    public record OverviewResponse(
            int playerCount,
            int signalCount,
            int highImpactSignalCount,
            List<LensResponse> lenses,
            List<PlayerResponse> players,
            List<SignalResponse> recentSignals,
            List<WeeklyBriefResponse> weeklyBriefs
    ) {
    }

    public record LensResponse(String code, String label, String description, long signalCount) {
    }

    public record PlayerResponse(Long id, String name, String layer, String country, String website, int watchPriority,
                                 boolean active) {
        public static PlayerResponse from(RadarPlayer player) {
            return new PlayerResponse(player.getId(), player.getName(), player.getLayer(), player.getCountry(),
                    player.getWebsite(), player.getWatchPriority(), player.isActive());
        }
    }

    public record PlayerUpdateRequest(
            @URL(protocol = "https") @Size(max = 500) String website,
            @Min(1) @Max(10) int watchPriority,
            boolean active
    ) {
    }

    public record SignalResponse(
            Long id,
            String title,
            String fact,
            String sourceUrl,
            String sourceTier,
            String signalType,
            LocalDateTime occurredAt,
            LocalDateTime capturedAt,
            int confidenceScore,
            int impactScore,
            String status,
            List<String> lenses,
            List<String> players,
            AssessmentResponse assessment
    ) {
        public static SignalResponse from(RadarSignal signal) {
            return new SignalResponse(
                    signal.getId(), signal.getTitle(), signal.getFact(), signal.getSourceUrl(), signal.getSourceTier(),
                    signal.getSignalType(), signal.getOccurredAt(), signal.getCapturedAt(), signal.getConfidenceScore(),
                    signal.getImpactScore(), signal.getStatus(), signal.getLenses().stream().sorted().toList(),
                    signal.getPlayers().stream().map(RadarPlayer::getName).sorted().toList(),
                    AssessmentResponse.from(signal.getAssessment())
            );
        }
    }

    public record AssessmentResponse(
            String whatChanged,
            String industryStructureImpact,
            String mspOpportunity,
            String mspThreat,
            String structuralRisk,
            String recommendedAction,
            String deliveryModel,
            String pricingModel
    ) {
        public static AssessmentResponse from(RadarAssessment assessment) {
            if (assessment == null) return null;
            return new AssessmentResponse(assessment.getWhatChanged(), assessment.getIndustryStructureImpact(),
                    assessment.getMspOpportunity(), assessment.getMspThreat(), assessment.getStructuralRisk(),
                    assessment.getRecommendedAction(), assessment.getDeliveryModel(), assessment.getPricingModel());
        }
    }

    public record SignalRequest(
            @NotBlank @Size(max = 500) String title,
            @NotBlank @Size(max = 5000) String fact,
            @NotBlank @URL(protocol = "https") @Size(max = 2000) String sourceUrl,
            @NotBlank @Size(max = 40) String sourceTier,
            @NotBlank @Size(max = 80) String signalType,
            @NotNull LocalDateTime occurredAt,
            @Min(0) @Max(100) int confidenceScore,
            @Min(0) @Max(100) int impactScore,
            @NotEmpty Set<String> lenses,
            @NotEmpty Set<String> playerNames,
            @Valid @NotNull AssessmentRequest assessment
    ) {
    }

    public record AssessmentRequest(
            @NotBlank @Size(max = 5000) String whatChanged,
            @NotBlank @Size(max = 5000) String industryStructureImpact,
            @Size(max = 5000) String mspOpportunity,
            @Size(max = 5000) String mspThreat,
            @Size(max = 5000) String structuralRisk,
            @NotBlank @Size(max = 5000) String recommendedAction,
            @Size(max = 500) String deliveryModel,
            @Size(max = 500) String pricingModel
    ) {
    }

    public record WeeklyBriefResponse(
            Long id,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String title,
            String executiveSummary,
            String playerMoves,
            String partnershipChanges,
            String deliveryModelChanges,
            String pricingChanges,
            String agenticOperationsChanges,
            String koreaImpact,
            LocalDateTime generatedAt
    ) {
        public static WeeklyBriefResponse from(RadarWeeklyBrief brief) {
            return new WeeklyBriefResponse(brief.getId(), brief.getPeriodStart(), brief.getPeriodEnd(), brief.getTitle(),
                    brief.getExecutiveSummary(), brief.getPlayerMoves(), brief.getPartnershipChanges(),
                    brief.getDeliveryModelChanges(), brief.getPricingChanges(), brief.getAgenticOperationsChanges(),
                    brief.getKoreaImpact(), brief.getGeneratedAt());
        }
    }
}
