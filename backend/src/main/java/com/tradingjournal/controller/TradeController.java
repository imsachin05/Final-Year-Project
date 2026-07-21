package com.tradingjournal.controller;

import com.tradingjournal.dto.TradeRequest;
import com.tradingjournal.dto.TradeResponse;
import com.tradingjournal.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getAllTrades() {
        return ResponseEntity.ok(tradeService.getAllTrades());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable Long id) {
        return ResponseEntity.ok(tradeService.getTrade(id));
    }

    @PostMapping
    public ResponseEntity<TradeResponse> createTrade(@Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.createTrade(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TradeResponse> updateTrade(@PathVariable Long id, @Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.updateTrade(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
        return ResponseEntity.noContent().build();
    }
}
