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
public class TradeOutlookResponse {
    private String symbol;
    private String strategy;
    private long matchingTradeCount;
    private double winRatePercent;
    private BigDecimal averagePnl;
    private double averageRiskRewardRatio;
    private String narrative; // AI-generated reflection, grounded strictly in the stats above
}
