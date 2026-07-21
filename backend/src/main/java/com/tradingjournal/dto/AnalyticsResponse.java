package com.tradingjournal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private long totalTrades;
    private long closedTrades;
    private long winningTrades;
    private long losingTrades;
    private double winRatePercent;
    private BigDecimal totalPnl;
    private BigDecimal averageWin;
    private BigDecimal averageLoss;
    private double profitFactor;
    private double expectancy;
    private double averageRiskRewardRatio;
    private BigDecimal maxDrawdown;
    private int currentStreak; // positive = win streak, negative = loss streak

    private List<EquityPoint> equityCurve;
    private List<MonthlyPnl> monthlyPnl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquityPoint {
        private String date;
        private BigDecimal cumulativePnl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPnl {
        private String month; // e.g. "2026-01"
        private BigDecimal pnl;
    }
}
