package com.tradingjournal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TradeStatus status = TradeStatus.OPEN;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal entryPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal exitPrice;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal stopLoss;

    @Column(precision = 19, scale = 4)
    private BigDecimal takeProfit;

    @Column(nullable = false)
    private LocalDateTime entryDate;

    private LocalDateTime exitDate;

    private String strategy;

    @Column(length = 2000)
    private String notes;
}
