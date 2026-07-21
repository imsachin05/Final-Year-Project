package com.tradingjournal.controller;

import com.tradingjournal.ai.AiCoachService;
import com.tradingjournal.dto.*;
import com.tradingjournal.service.AnalyticsService;
import com.tradingjournal.service.RiskCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All AI-powered endpoints for the app. Each one maps to a feature shown on the
 * "AI Dashboard" page in the frontend:
 *  1. AI Trade Coach       -> POST /api/ai/trade-feedback/{id}
 *  2. AI Trade Rating      -> POST /api/ai/trade-rating/{id}
 *  3. AI Emotion Detection -> GET  /api/ai/emotion-analysis
 *  4. AI Weekly Report     -> GET  /api/ai/weekly-report
 *  5. AI Journal Summary   -> GET  /api/ai/journal-summary
 *  6. AI Strategy Analyzer -> GET  /api/ai/strategy-analysis (narrative) + GET /api/analytics/by-strategy (raw numbers)
 *  7. AI Risk Calculator   -> POST /api/risk/calculate (deterministic) + POST /api/ai/risk-tip (AI commentary)
 *  8. AI Trade Outlook     -> GET  /api/ai/trade-outlook (historical pattern match, not a prediction)
 *  9. AI Chat Assistant    -> POST /api/ai/ask
 * 10. AI Dashboard          -> frontend page combining all of the above
 */
@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiCoachService aiCoachService;
    private final AnalyticsService analyticsService;
    private final RiskCalculatorService riskCalculatorService;

    // ---- 1. AI Trade Coach ----
    @PostMapping("/api/ai/trade-feedback/{tradeId}")
    public ResponseEntity<AiResponse> tradeFeedback(@PathVariable Long tradeId) {
        return ResponseEntity.ok(new AiResponse(aiCoachService.tradeFeedback(tradeId)));
    }

    // ---- 2. AI Trade Rating ----
    @PostMapping("/api/ai/trade-rating/{tradeId}")
    public ResponseEntity<AiResponse> tradeRating(@PathVariable Long tradeId) {
        return ResponseEntity.ok(new AiResponse(aiCoachService.rateTrade(tradeId)));
    }

    // ---- 3. AI Emotion Detection ----
    @GetMapping("/api/ai/emotion-analysis")
    public ResponseEntity<AiResponse> emotionAnalysis() {
        return ResponseEntity.ok(new AiResponse(aiCoachService.emotionAnalysis()));
    }

    // ---- 4. AI Weekly Report ----
    @GetMapping("/api/ai/weekly-report")
    public ResponseEntity<AiResponse> weeklyReport() {
        return ResponseEntity.ok(new AiResponse(aiCoachService.weeklyReport()));
    }

    // ---- 5. AI Journal Summary ----
    @GetMapping("/api/ai/journal-summary")
    public ResponseEntity<AiResponse> journalSummary() {
        return ResponseEntity.ok(new AiResponse(aiCoachService.journalSummary()));
    }

    // ---- 6. AI Strategy Analyzer ----
    @GetMapping("/api/analytics/by-strategy")
    public ResponseEntity<StrategyAnalyticsResponse> strategyBreakdown() {
        return ResponseEntity.ok(analyticsService.getStrategyBreakdown());
    }

    @GetMapping("/api/ai/strategy-analysis")
    public ResponseEntity<AiResponse> strategyAnalysis() {
        return ResponseEntity.ok(new AiResponse(aiCoachService.strategyAnalysisNarrative()));
    }

    // ---- 7. AI Risk Calculator ----
    @PostMapping("/api/risk/calculate")
    public ResponseEntity<RiskCalcResponse> calculateRisk(@Valid @RequestBody RiskCalcRequest request) {
        return ResponseEntity.ok(riskCalculatorService.calculate(request));
    }

    @PostMapping("/api/ai/risk-tip")
    public ResponseEntity<AiResponse> riskTip(@Valid @RequestBody RiskCalcRequest request) {
        RiskCalcResponse result = riskCalculatorService.calculate(request);
        return ResponseEntity.ok(new AiResponse(aiCoachService.riskTip(request, result)));
    }

    // ---- 8. AI Trade Outlook (historical pattern match — not a market prediction) ----
    @GetMapping("/api/ai/trade-outlook")
    public ResponseEntity<TradeOutlookResponse> tradeOutlook(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String strategy) {

        TradeOutlookResponse stats = analyticsService.getOutlookStats(symbol, strategy);
        String narrative = aiCoachService.tradeOutlookNarrative(stats);
        stats.setNarrative(narrative);
        return ResponseEntity.ok(stats);
    }

    // ---- 9. AI Chat Assistant ----
    @PostMapping("/api/ai/ask")
    public ResponseEntity<AiResponse> ask(@Valid @RequestBody AiAskRequest request) {
        return ResponseEntity.ok(new AiResponse(aiCoachService.ask(request.getQuestion())));
    }

    // ---- Bonus: dashboard-level "big picture" insights, feeds the Dashboard's AI Insights card ----
    @GetMapping("/api/ai/insights")
    public ResponseEntity<AiResponse> insights() {
        return ResponseEntity.ok(new AiResponse(aiCoachService.getInsights()));
    }
}
