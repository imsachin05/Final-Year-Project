package com.tradingjournal.config;

import com.tradingjournal.model.Trade;
import com.tradingjournal.model.TradeStatus;
import com.tradingjournal.model.TradeType;
import com.tradingjournal.model.User;
import com.tradingjournal.repository.TradeRepository;
import com.tradingjournal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds a demo account with realistic-looking sample trades so the dashboard
 * and charts are populated immediately for screenshots, your report, and your viva demo.
 *
 * Login with username "demo" / password "demo1234" after starting the backend.
 *
 * To disable (e.g. before final submission if you want a clean/empty state),
 * set app.demo-data.enabled=false in application.properties.
 */
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-data.enabled:true}")
    private boolean enabled;

    private static final String[] SYMBOLS = {"AAPL", "TSLA", "BTCUSDT", "ETHUSDT", "NIFTY50", "RELIANCE", "MSFT", "NVDA"};
    private static final String[] STRATEGIES = {"Breakout", "Trend Following", "Scalping", "Mean Reversion", "News Trade"};

    @Override
    public void run(String... args) {
        if (!enabled) return;
        if (userRepository.existsByUsername("demo")) return;

        User demoUser = User.builder()
                .username("demo")
                .email("demo@finalyearproject.app")
                .password(passwordEncoder.encode("demo1234"))
                .build();
        userRepository.save(demoUser);

        Random random = new Random(42); // fixed seed = reproducible demo data
        LocalDateTime cursor = LocalDateTime.now().minusDays(75);

        for (int i = 0; i < 40; i++) {
            String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            TradeType type = random.nextBoolean() ? TradeType.LONG : TradeType.SHORT;
            String strategy = STRATEGIES[random.nextInt(STRATEGIES.length)];

            BigDecimal entryPrice = BigDecimal.valueOf(50 + random.nextDouble() * 450).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal quantity = BigDecimal.valueOf(1 + random.nextInt(20));

            // Bias slightly toward wins to show a healthy but realistic journal (~58% win rate)
            boolean isWin = random.nextDouble() < 0.58;
            double movePercent = (2 + random.nextDouble() * 6) / 100.0; // 2%-8% move
            BigDecimal exitPrice;
            if (type == TradeType.LONG) {
                exitPrice = isWin
                        ? entryPrice.multiply(BigDecimal.valueOf(1 + movePercent))
                        : entryPrice.multiply(BigDecimal.valueOf(1 - movePercent * 0.6));
            } else {
                exitPrice = isWin
                        ? entryPrice.multiply(BigDecimal.valueOf(1 - movePercent))
                        : entryPrice.multiply(BigDecimal.valueOf(1 + movePercent * 0.6));
            }
            exitPrice = exitPrice.setScale(2, java.math.RoundingMode.HALF_UP);

            BigDecimal stopLoss = type == TradeType.LONG
                    ? entryPrice.multiply(BigDecimal.valueOf(0.95)).setScale(2, java.math.RoundingMode.HALF_UP)
                    : entryPrice.multiply(BigDecimal.valueOf(1.05)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal takeProfit = type == TradeType.LONG
                    ? entryPrice.multiply(BigDecimal.valueOf(1.08)).setScale(2, java.math.RoundingMode.HALF_UP)
                    : entryPrice.multiply(BigDecimal.valueOf(0.92)).setScale(2, java.math.RoundingMode.HALF_UP);

            cursor = cursor.plusHours(6 + random.nextInt(48));
            LocalDateTime entryDate = cursor;
            LocalDateTime exitDate = entryDate.plusHours(1 + random.nextInt(30));

            Trade trade = Trade.builder()
                    .user(demoUser)
                    .symbol(symbol)
                    .tradeType(type)
                    .status(TradeStatus.CLOSED)
                    .entryPrice(entryPrice)
                    .exitPrice(exitPrice)
                    .quantity(quantity)
                    .stopLoss(stopLoss)
                    .takeProfit(takeProfit)
                    .entryDate(entryDate)
                    .exitDate(exitDate)
                    .strategy(strategy)
                    .notes("Demo trade generated for showcase purposes.")
                    .build();

            tradeRepository.save(trade);
        }

        // A couple of still-open positions so the OPEN badge/status also shows in the demo
        for (String symbol : List.of("AAPL", "BTCUSDT")) {
            Trade openTrade = Trade.builder()
                    .user(demoUser)
                    .symbol(symbol)
                    .tradeType(TradeType.LONG)
                    .status(TradeStatus.OPEN)
                    .entryPrice(BigDecimal.valueOf(150 + random.nextInt(100)))
                    .quantity(BigDecimal.valueOf(5))
                    .stopLoss(BigDecimal.valueOf(140))
                    .takeProfit(BigDecimal.valueOf(180))
                    .entryDate(LocalDateTime.now().minusHours(4))
                    .strategy("Trend Following")
                    .notes("Still open — waiting for target.")
                    .build();
            tradeRepository.save(openTrade);
        }
    }
}
