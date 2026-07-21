package com.tradingjournal.service;

import com.tradingjournal.dto.RiskCalcRequest;
import com.tradingjournal.dto.RiskCalcResponse;
import com.tradingjournal.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure arithmetic position-sizing calculator — deliberately NOT an AI call.
 * Implements the standard fixed-fractional risk model traders use:
 * position size = (account balance * risk %) / (entry price - stop loss price).
 * An optional AI-generated commentary on top of this result is available via
 * AiCoachService.riskTip(), kept as a separate step so the core numbers never
 * depend on an AI provider being configured.
 */
@Service
public class RiskCalculatorService {

    public RiskCalcResponse calculate(RiskCalcRequest request) {
        BigDecimal riskPerUnit = request.getEntryPrice().subtract(request.getStopLossPrice()).abs();

        if (riskPerUnit.compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException("Entry price and stop loss price can't be the same.");
        }

        BigDecimal riskAmount = request.getAccountBalance()
                .multiply(request.getRiskPercentage())
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        BigDecimal suggestedQuantity = riskAmount.divide(riskPerUnit, 4, RoundingMode.HALF_UP);
        BigDecimal positionValue = suggestedQuantity.multiply(request.getEntryPrice()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal riskRewardRatio = null;
        if (request.getTakeProfitPrice() != null) {
            BigDecimal reward = request.getTakeProfitPrice().subtract(request.getEntryPrice()).abs();
            if (riskPerUnit.compareTo(BigDecimal.ZERO) != 0) {
                riskRewardRatio = reward.divide(riskPerUnit, 2, RoundingMode.HALF_UP);
            }
        }

        return RiskCalcResponse.builder()
                .riskAmount(riskAmount.setScale(2, RoundingMode.HALF_UP))
                .riskPerUnit(riskPerUnit.setScale(4, RoundingMode.HALF_UP))
                .suggestedQuantity(suggestedQuantity)
                .positionValue(positionValue)
                .riskRewardRatio(riskRewardRatio)
                .build();
    }
}
