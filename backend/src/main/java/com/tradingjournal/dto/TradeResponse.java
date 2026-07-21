package com.tradingjournal.dto;

import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.model.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {
    private Long id;
    private String symbol;
    private TradeType tradeType;
    private TradeStatus status;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal quantity;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;
    private String strategy;
    private String notes;

    // Derived / calculated fields
    private BigDecimal pnl;
    private BigDecimal riskRewardRatio;
    private boolean win;
}
