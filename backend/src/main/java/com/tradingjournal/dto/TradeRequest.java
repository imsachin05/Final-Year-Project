package com.tradingjournal.dto;

import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.model.TradeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeRequest {
    @NotBlank
    private String symbol;

    @NotNull
    private TradeType tradeType;

    private TradeStatus status;

    @NotNull
    private BigDecimal entryPrice;

    private BigDecimal exitPrice;

    @NotNull
    private BigDecimal quantity;

    private BigDecimal stopLoss;

    private BigDecimal takeProfit;

    @NotNull
    private LocalDateTime entryDate;

    private LocalDateTime exitDate;

    private String strategy;

    private String notes;
}
