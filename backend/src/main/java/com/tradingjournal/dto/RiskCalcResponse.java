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
public class RiskCalcResponse {
    private BigDecimal riskAmount;       // money at risk = accountBalance * riskPercentage%
    private BigDecimal riskPerUnit;      // |entryPrice - stopLossPrice|
    private BigDecimal suggestedQuantity; // riskAmount / riskPerUnit
    private BigDecimal positionValue;    // suggestedQuantity * entryPrice
    private BigDecimal riskRewardRatio;  // only if takeProfitPrice was provided
}
