package com.aimsp.intelligence.dto;

import com.aimsp.intelligence.domain.strategy.StrategyReport;

import java.time.LocalDateTime;

public final class StrategyReportDto {

    private StrategyReportDto() {
    }

    public record Response(
            Long id,
            String title,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            String executiveSummary,
            String valueChainImpact,
            String fdeDeliveryAnalysis,
            String pricingModelAnalysis,
            String agenticOpsAnalysis,
            String mspOpportunitiesThreats,
            String top3Actions,
            int sourceSignalCount,
            LocalDateTime generatedAt
    ) {
        public static Response from(StrategyReport report) {
            return new Response(
                    report.getId(),
                    report.getTitle(),
                    report.getPeriodStart(),
                    report.getPeriodEnd(),
                    report.getExecutiveSummary(),
                    report.getValueChainImpact(),
                    report.getFdeDeliveryAnalysis(),
                    report.getPricingModelAnalysis(),
                    report.getAgenticOpsAnalysis(),
                    report.getMspOpportunitiesThreats(),
                    report.getTop3Actions(),
                    report.getSourceSignalCount(),
                    report.getGeneratedAt()
            );
        }
    }
}
