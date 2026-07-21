package com.tradingjournal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RiskCalcRequest {
    @NotNull @Positive
    private BigDecimal accountBalance;

    @NotNull @Positive
    private BigDecimal riskPercentage; // e.g. 1 = risk 1% of account per trade

    @NotNull @Positive
    private BigDecimal entryPrice;

    @NotNull @Positive
    private BigDecimal stopLossPrice;

    private BigDecimal takeProfitPrice; // optional, used to also show resulting R:R
}
