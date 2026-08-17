package com.aimsp.intelligence.domain.strategy;

import com.aimsp.intelligence.ai.GeminiWorkCoordinator;
import com.aimsp.intelligence.ai.StrategyReportGenerator;
import com.aimsp.intelligence.domain.radar.RadarSignal;
import com.aimsp.intelligence.domain.radar.RadarSignalRepository;
import com.aimsp.intelligence.domain.radar.RadarSourceVerificationService;
import com.aimsp.intelligence.dto.PageResponseDto;
import com.aimsp.intelligence.dto.StrategyReportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyReportService {

    private final StrategyReportRepository strategyReportRepository;
    private final RadarSignalRepository radarSignalRepository;
    private final StrategyReportGenerator strategyReportGenerator;
    private final GeminiWorkCoordinator geminiWorkCoordinator;

    @Transactional(readOnly = true)
    public PageResponseDto<StrategyReportDto.Response> getReports(Pageable pageable) {
        Page<StrategyReport> page = strategyReportRepository.findAllByOrderByGeneratedAtDesc(pageable);
        return PageResponseDto.from(page.map(StrategyReportDto.Response::from));
    }

    @Transactional(readOnly = true)
    public StrategyReportDto.Response getReport(Long id) {
        StrategyReport report = strategyReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("전략 보고서를 찾을 수 없습니다: " + id));
        return StrategyReportDto.Response.from(report);
    }

    @Transactional(readOnly = true)
    public StrategyReportDto.Response getLatestReport() {
        return strategyReportRepository.findTopByOrderByGeneratedAtDesc()
                .map(StrategyReportDto.Response::from)
                .orElse(null);
    }

    public StrategyReportDto.Response generateReport() {
        return geminiWorkCoordinator.executeExclusive("AI 서비스 데일리 브리핑 생성", this::generateReportInternal);
    }

    @Transactional
    public StrategyReportDto.Response generateReportInternal() {
        LocalDateTime periodEnd = LocalDateTime.now();
        LocalDateTime periodStart = periodEnd.minusDays(14);

        List<RadarSignal> signals = radarSignalRepository
                .findByOccurredAtBetweenOrderByImpactScoreDescOccurredAtDesc(periodStart, periodEnd).stream()
                .filter(signal -> !RadarSourceVerificationService.SOURCE_UNAVAILABLE.equals(signal.getStatus()))
                .toList();

        // 14일간 신호가 부족하면 전체 유효 신호 중 상위 15건 활용
        if (signals.isEmpty()) {
            signals = radarSignalRepository
                    .findTop12ByStatusNotOrderByOccurredAtDescCapturedAtDesc(RadarSourceVerificationService.SOURCE_UNAVAILABLE);
        }

        if (signals.isEmpty()) {
            throw new IllegalArgumentException("전략 보고서를 작성할 검증 신호가 존재하지 않습니다.");
        }

        StrategyReportGenerator.ReportResult result = strategyReportGenerator.generateReport(signals, periodStart, periodEnd);
        if (result == null) {
            throw new IllegalStateException("전략 보고서 생성에 실패했습니다. Gemini API 응답을 확인하세요.");
        }

        StrategyReport report = new StrategyReport();
        report.setTitle(result.title());
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setExecutiveSummary(result.executiveSummary());
        report.setValueChainImpact(result.valueChainImpact());
        report.setFdeDeliveryAnalysis(result.fdeDeliveryAnalysis());
        report.setPricingModelAnalysis(result.pricingModelAnalysis());
        report.setAgenticOpsAnalysis(result.agenticOpsAnalysis());
        report.setMspOpportunitiesThreats(result.mspOpportunitiesThreats());
        report.setTop3Actions(result.top3Actions());
        report.setSourceSignalCount(signals.size());
        report.setGeneratedAt(LocalDateTime.now());

        StrategyReport saved = strategyReportRepository.save(report);
        log.info("신규 전략 보고서 생성 완료: id={}, title={}", saved.getId(), saved.getTitle());
        return StrategyReportDto.Response.from(saved);
    }
}
