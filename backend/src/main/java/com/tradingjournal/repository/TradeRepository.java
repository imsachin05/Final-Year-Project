package com.tradingjournal.repository;

import com.tradingjournal.model.Trade;
import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserOrderByEntryDateDesc(User user);
    List<Trade> findByUserAndStatusOrderByExitDateAsc(User user, TradeStatus status);
    List<Trade> findByUserIdAndId(Long userId, Long id);
}
