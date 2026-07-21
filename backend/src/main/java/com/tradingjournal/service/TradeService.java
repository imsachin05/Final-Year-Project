package com.tradingjournal.service;

import com.tradingjournal.dto.TradeRequest;
import com.tradingjournal.dto.TradeResponse;
import com.tradingjournal.exception.ResourceNotFoundException;
import com.tradingjournal.model.Trade;
import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.model.TradeType;
import com.tradingjournal.model.User;
import com.tradingjournal.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;

    public User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<TradeResponse> getAllTrades() {
        return tradeRepository.findByUserOrderByEntryDateDesc(currentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TradeResponse getTrade(Long id) {
        Trade trade = findOwnedTrade(id);
        return toResponse(trade);
    }

    public TradeResponse createTrade(TradeRequest request) {
        Trade trade = Trade.builder()
                .user(currentUser())
                .symbol(request.getSymbol().toUpperCase())
                .tradeType(request.getTradeType())
                .status(request.getStatus() != null ? request.getStatus()
                        : (request.getExitPrice() != null ? TradeStatus.CLOSED : TradeStatus.OPEN))
                .entryPrice(request.getEntryPrice())
                .exitPrice(request.getExitPrice())
                .quantity(request.getQuantity())
                .stopLoss(request.getStopLoss())
                .takeProfit(request.getTakeProfit())
                .entryDate(request.getEntryDate())
                .exitDate(request.getExitDate())
                .strategy(request.getStrategy())
                .notes(request.getNotes())
                .build();

        return toResponse(tradeRepository.save(trade));
    }

    public TradeResponse updateTrade(Long id, TradeRequest request) {
        Trade trade = findOwnedTrade(id);

        trade.setSymbol(request.getSymbol().toUpperCase());
        trade.setTradeType(request.getTradeType());
        trade.setEntryPrice(request.getEntryPrice());
        trade.setExitPrice(request.getExitPrice());
        trade.setQuantity(request.getQuantity());
        trade.setStopLoss(request.getStopLoss());
        trade.setTakeProfit(request.getTakeProfit());
        trade.setEntryDate(request.getEntryDate());
        trade.setExitDate(request.getExitDate());
        trade.setStrategy(request.getStrategy());
        trade.setNotes(request.getNotes());
        trade.setStatus(request.getStatus() != null ? request.getStatus()
                : (request.getExitPrice() != null ? TradeStatus.CLOSED : TradeStatus.OPEN));

        return toResponse(tradeRepository.save(trade));
    }

    public void deleteTrade(Long id) {
        Trade trade = findOwnedTrade(id);
        tradeRepository.delete(trade);
    }

    private Trade findOwnedTrade(Long id) {
        return tradeRepository.findByUserIdAndId(currentUser().getId(), id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Trade not found with id " + id));
    }

    public TradeResponse toResponse(Trade t) {
        BigDecimal pnl = calculatePnl(t);
        BigDecimal rr = calculateRiskReward(t);

        return TradeResponse.builder()
                .id(t.getId())
                .symbol(t.getSymbol())
                .tradeType(t.getTradeType())
                .status(t.getStatus())
                .entryPrice(t.getEntryPrice())
                .exitPrice(t.getExitPrice())
                .quantity(t.getQuantity())
                .stopLoss(t.getStopLoss())
                .takeProfit(t.getTakeProfit())
                .entryDate(t.getEntryDate())
                .exitDate(t.getExitDate())
                .strategy(t.getStrategy())
                .notes(t.getNotes())
                .pnl(pnl)
                .riskRewardRatio(rr)
                .win(pnl != null && pnl.compareTo(BigDecimal.ZERO) > 0)
                .build();
    }

    public static BigDecimal calculatePnl(Trade t) {
        if (t.getExitPrice() == null || t.getStatus() != TradeStatus.CLOSED) {
            return null;
        }
        BigDecimal diff = t.getTradeType() == TradeType.LONG
                ? t.getExitPrice().subtract(t.getEntryPrice())
                : t.getEntryPrice().subtract(t.getExitPrice());

        return diff.multiply(t.getQuantity()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateRiskReward(Trade t) {
        if (t.getStopLoss() == null || t.getTakeProfit() == null) {
            return null;
        }
        BigDecimal risk = t.getEntryPrice().subtract(t.getStopLoss()).abs();
        BigDecimal reward = t.getTakeProfit().subtract(t.getEntryPrice()).abs();

        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return reward.divide(risk, 2, RoundingMode.HALF_UP);
    }
}
