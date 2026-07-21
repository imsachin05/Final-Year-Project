package com.tradingjournal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyStat {
    private String strategy;
    private long totalTrades;
    private long winningTrades;
    private double winRatePercent;
    private BigDecimal totalPnl;
    private BigDecimal averagePnl;
    private double averageRiskRewardRatio;
}
