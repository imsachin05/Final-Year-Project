package com.tradingjournal.service;

import com.tradingjournal.dto.AnalyticsResponse;
import com.tradingjournal.dto.StrategyAnalyticsResponse;
import com.tradingjournal.dto.StrategyStat;
import com.tradingjournal.dto.TradeOutlookResponse;
import com.tradingjournal.model.Trade;
import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TradeRepository tradeRepository;
    private final TradeService tradeService;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AnalyticsResponse getAnalytics() {
        List<Trade> closedTrades = tradeRepository
                .findByUserAndStatusOrderByExitDateAsc(tradeService.currentUser(), TradeStatus.CLOSED);

        long totalTrades = tradeRepository.findByUserOrderByEntryDateDesc(tradeService.currentUser()).size();
        long closedCount = closedTrades.size();

        if (closedCount == 0) {
            return AnalyticsResponse.builder()
                    .totalTrades(totalTrades)
                    .closedTrades(0)
                    .winningTrades(0)
                    .losingTrades(0)
                    .winRatePercent(0)
                    .totalPnl(BigDecimal.ZERO)
                    .averageWin(BigDecimal.ZERO)
                    .averageLoss(BigDecimal.ZERO)
                    .profitFactor(0)
                    .expectancy(0)
                    .averageRiskRewardRatio(0)
                    .maxDrawdown(BigDecimal.ZERO)
                    .currentStreak(0)
                    .equityCurve(new ArrayList<>())
                    .monthlyPnl(new ArrayList<>())
                    .build();
        }

        long wins = 0;
        long losses = 0;
        BigDecimal totalPnl = BigDecimal.ZERO;
        BigDecimal sumWins = BigDecimal.ZERO;
        BigDecimal sumLosses = BigDecimal.ZERO; // stored as positive number
        BigDecimal rrSum = BigDecimal.ZERO;
        int rrCount = 0;

        BigDecimal runningEquity = BigDecimal.ZERO;
        BigDecimal peakEquity = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        List<AnalyticsResponse.EquityPoint> equityCurve = new ArrayList<>();
        Map<String, BigDecimal> monthlyMap = new LinkedHashMap<>();

        int streak = 0;
        Boolean lastWasWin = null;

        for (Trade t : closedTrades) {
            BigDecimal pnl = TradeService.calculatePnl(t);
            if (pnl == null) continue;

            totalPnl = totalPnl.add(pnl);
            boolean isWin = pnl.compareTo(BigDecimal.ZERO) > 0;

            if (isWin) {
                wins++;
                sumWins = sumWins.add(pnl);
            } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
                losses++;
                sumLosses = sumLosses.add(pnl.abs());
            }

            BigDecimal rr = TradeService.calculateRiskReward(t);
            if (rr != null) {
                rrSum = rrSum.add(rr);
                rrCount++;
            }

            // Equity curve + drawdown
            runningEquity = runningEquity.add(pnl);
            if (runningEquity.compareTo(peakEquity) > 0) {
                peakEquity = runningEquity;
            }
            BigDecimal drawdown = peakEquity.subtract(runningEquity);
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }

            String dateLabel = t.getExitDate() != null ? t.getExitDate().format(DATE_FORMAT) : "";
            equityCurve.add(AnalyticsResponse.EquityPoint.builder()
                    .date(dateLabel)
                    .cumulativePnl(runningEquity.setScale(2, RoundingMode.HALF_UP))
                    .build());

            // Monthly aggregation
            if (t.getExitDate() != null) {
                String monthKey = t.getExitDate().format(MONTH_FORMAT);
                monthlyMap.merge(monthKey, pnl, BigDecimal::add);
            }

            // Streak: current streak counts from the most recent trades backward,
            // but since we're iterating forward chronologically, track running streak
            // and keep the final value (streak as of the most recent closed trade).
            if (lastWasWin == null || lastWasWin == isWin) {
                streak = isWin ? Math.max(streak, 0) + 1 : Math.min(streak, 0) - 1;
            } else {
                streak = isWin ? 1 : -1;
            }
            lastWasWin = isWin;
        }

        double winRate = closedCount == 0 ? 0 : (wins * 100.0) / closedCount;
        BigDecimal avgWin = wins == 0 ? BigDecimal.ZERO : sumWins.divide(BigDecimal.valueOf(wins), 2, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses == 0 ? BigDecimal.ZERO : sumLosses.divide(BigDecimal.valueOf(losses), 2, RoundingMode.HALF_UP);
        // Note: Double.POSITIVE_INFINITY is not valid JSON, so when there are no losing
        // trades yet we cap profit factor at a sentinel value (999) rather than serializing
        // Infinity, which would break the frontend's JSON parsing.
        double profitFactor = sumLosses.compareTo(BigDecimal.ZERO) == 0
                ? (sumWins.compareTo(BigDecimal.ZERO) > 0 ? 999.0 : 0)
                : sumWins.divide(sumLosses, 4, RoundingMode.HALF_UP).doubleValue();

        double lossRate = 1 - (winRate / 100.0);
        double expectancy = ((winRate / 100.0) * avgWin.doubleValue()) - (lossRate * avgLoss.doubleValue());

        double avgRR = rrCount == 0 ? 0 : rrSum.divide(BigDecimal.valueOf(rrCount), 2, RoundingMode.HALF_UP).doubleValue();

        List<AnalyticsResponse.MonthlyPnl> monthlyPnl = new ArrayList<>();
        monthlyMap.forEach((month, pnl) -> monthlyPnl.add(
                AnalyticsResponse.MonthlyPnl.builder()
                        .month(month)
                        .pnl(pnl.setScale(2, RoundingMode.HALF_UP))
                        .build()
        ));

        return AnalyticsResponse.builder()
                .totalTrades(totalTrades)
                .closedTrades(closedCount)
                .winningTrades(wins)
                .losingTrades(losses)
                .winRatePercent(Math.round(winRate * 100.0) / 100.0)
                .totalPnl(totalPnl.setScale(2, RoundingMode.HALF_UP))
                .averageWin(avgWin)
                .averageLoss(avgLoss)
                .profitFactor(Math.round(profitFactor * 100.0) / 100.0)
                .expectancy(Math.round(expectancy * 100.0) / 100.0)
                .averageRiskRewardRatio(avgRR)
                .maxDrawdown(maxDrawdown.setScale(2, RoundingMode.HALF_UP))
                .currentStreak(streak)
                .equityCurve(equityCurve)
                .monthlyPnl(monthlyPnl)
                .build();
    }

    /**
     * Deterministic per-strategy performance breakdown (no AI involved) — powers the
     * Strategy Analyzer feature. AI is layered on top of this data separately, in
     * AiCoachService, purely to narrate what these numbers already show.
     */
    public StrategyAnalyticsResponse getStrategyBreakdown() {
        List<Trade> closedTrades = tradeRepository
                .findByUserAndStatusOrderByExitDateAsc(tradeService.currentUser(), TradeStatus.CLOSED);

        Map<String, List<Trade>> byStrategy = new LinkedHashMap<>();
        for (Trade t : closedTrades) {
            String key = (t.getStrategy() == null || t.getStrategy().isBlank()) ? "Untagged" : t.getStrategy();
            byStrategy.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        List<StrategyStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<Trade>> entry : byStrategy.entrySet()) {
            List<Trade> trades = entry.getValue();
            long total = trades.size();
            long wins = 0;
            BigDecimal totalPnl = BigDecimal.ZERO;
            BigDecimal rrSum = BigDecimal.ZERO;
            int rrCount = 0;

            for (Trade t : trades) {
                BigDecimal pnl = TradeService.calculatePnl(t);
                if (pnl == null) continue;
                totalPnl = totalPnl.add(pnl);
                if (pnl.compareTo(BigDecimal.ZERO) > 0) wins++;

                BigDecimal rr = TradeService.calculateRiskReward(t);
                if (rr != null) {
                    rrSum = rrSum.add(rr);
                    rrCount++;
                }
            }

            double winRate = total == 0 ? 0 : Math.round((wins * 10000.0) / total) / 100.0;
            BigDecimal avgPnl = total == 0 ? BigDecimal.ZERO
                    : totalPnl.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            double avgRR = rrCount == 0 ? 0
                    : rrSum.divide(BigDecimal.valueOf(rrCount), 2, RoundingMode.HALF_UP).doubleValue();

            stats.add(StrategyStat.builder()
                    .strategy(entry.getKey())
                    .totalTrades(total)
                    .winningTrades(wins)
                    .winRatePercent(winRate)
                    .totalPnl(totalPnl.setScale(2, RoundingMode.HALF_UP))
                    .averagePnl(avgPnl)
                    .averageRiskRewardRatio(avgRR)
                    .build());
        }

        // Best strategies first by total P/L, so the table is useful at a glance
        stats.sort((a, b) -> b.getTotalPnl().compareTo(a.getTotalPnl()));

        return StrategyAnalyticsResponse.builder().stats(stats).build();
    }

    /**
     * Deterministic stats for trades matching a given symbol and/or strategy — powers the
     * "AI Trade Outlook" feature. This is historical pattern-matching on the user's OWN
     * past trades, not a market prediction. The narrative layer (added separately by
     * AiCoachService) must stay grounded in exactly these numbers.
     */
    public TradeOutlookResponse getOutlookStats(String symbol, String strategy) {
        List<Trade> closedTrades = tradeRepository
                .findByUserAndStatusOrderByExitDateAsc(tradeService.currentUser(), TradeStatus.CLOSED);

        List<Trade> matching = closedTrades.stream()
                .filter(t -> symbol == null || symbol.isBlank() || t.getSymbol().equalsIgnoreCase(symbol.trim()))
                .filter(t -> strategy == null || strategy.isBlank() || strategy.equalsIgnoreCase("Untagged")
                        ? (t.getStrategy() == null || t.getStrategy().isBlank())
                        : t.getStrategy() != null && t.getStrategy().equalsIgnoreCase(strategy.trim()))
                .toList();

        long total = matching.size();
        long wins = 0;
        BigDecimal totalPnl = BigDecimal.ZERO;
        BigDecimal rrSum = BigDecimal.ZERO;
        int rrCount = 0;

        for (Trade t : matching) {
            BigDecimal pnl = TradeService.calculatePnl(t);
            if (pnl == null) continue;
            totalPnl = totalPnl.add(pnl);
            if (pnl.compareTo(BigDecimal.ZERO) > 0) wins++;

            BigDecimal rr = TradeService.calculateRiskReward(t);
            if (rr != null) {
                rrSum = rrSum.add(rr);
                rrCount++;
            }
        }

        double winRate = total == 0 ? 0 : Math.round((wins * 10000.0) / total) / 100.0;
        BigDecimal avgPnl = total == 0 ? BigDecimal.ZERO
                : totalPnl.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        double avgRR = rrCount == 0 ? 0
                : rrSum.divide(BigDecimal.valueOf(rrCount), 2, RoundingMode.HALF_UP).doubleValue();

        return TradeOutlookResponse.builder()
                .symbol(symbol)
                .strategy(strategy)
                .matchingTradeCount(total)
                .winRatePercent(winRate)
                .averagePnl(avgPnl)
                .averageRiskRewardRatio(avgRR)
                .build();
    }

    /** Closed trades from the last 7 days — powers the AI Weekly Report feature. */
    public List<Trade> getLastSevenDaysClosedTrades() {
        List<Trade> closedTrades = tradeRepository
                .findByUserAndStatusOrderByExitDateAsc(tradeService.currentUser(), TradeStatus.CLOSED);

        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(7);
        return closedTrades.stream()
                .filter(t -> t.getExitDate() != null && t.getExitDate().isAfter(cutoff))
                .toList();
    }
}
