package com.tradingjournal.ai;

import com.tradingjournal.dto.AnalyticsResponse;
import com.tradingjournal.dto.RiskCalcRequest;
import com.tradingjournal.dto.RiskCalcResponse;
import com.tradingjournal.dto.TradeOutlookResponse;
import com.tradingjournal.dto.TradeResponse;
import com.tradingjournal.model.Trade;
import com.tradingjournal.service.AnalyticsService;
import com.tradingjournal.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds prompts from the user's own trade history and analytics, then delegates
 * to AiClient for the actual model call. Kept deliberately scoped to *retrospective*
 * coaching (patterns, discipline, risk habits) rather than live trade recommendations —
 * this is a student project analyzing historical data the user already recorded,
 * not a financial-advice product.
 */
@Service
@RequiredArgsConstructor
public class AiCoachService {

    private static final int MAX_TRADES_IN_CONTEXT = 40;

    private static final String COACH_SYSTEM_PROMPT = """
            You are an assistant embedded in a personal trading journal app called "Final Year Project".
            You analyze a single trader's OWN historical, already-closed trades to help them reflect on
            their habits, discipline, and risk management. You are a journaling coach, not a financial advisor.

            Rules you must follow:
            - Never recommend a specific stock, coin, or instrument to buy or sell.
            - Never give live market predictions or price targets.
            - Base every observation strictly on the trade data provided; do not invent trades or numbers.
            - Focus on patterns: win rate by strategy/symbol, risk-reward discipline, emotional/behavioral
              cues visible in their notes (e.g. FOMO, revenge trading, ignoring stop losses), consistency,
              and overtrading.
            - Be direct and specific, citing the actual numbers you were given. Avoid generic platitudes.
            - Keep the tone constructive and non-judgmental — the goal is helping them improve, not scolding.
            - Format your answer as short, scannable sections with headers, not one big paragraph.
            """;

    private final TradeService tradeService;
    private final AnalyticsService analyticsService;
    private final AiClient aiClient;

    public String getInsights() {
        List<TradeResponse> trades = tradeService.getAllTrades();
        AnalyticsResponse analytics = analyticsService.getAnalytics();

        String context = buildContext(trades, analytics);

        String userPrompt = context + """


                Based only on the data above, give me:
                1. Two or three genuine STRENGTHS you can see in this trading behavior
                2. Two or three concrete WEAKNESSES or risk patterns
                3. Three specific, actionable suggestions to improve discipline going forward

                Keep it under 300 words.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    public String ask(String question) {
        List<TradeResponse> trades = tradeService.getAllTrades();
        AnalyticsResponse analytics = analyticsService.getAnalytics();

        String context = buildContext(trades, analytics);

        String userPrompt = context + """


                The trader is asking you the following question about their OWN trading history above.
                Answer using only the data provided. If the data doesn't contain enough information to
                answer confidently, say so plainly instead of guessing.

                Question: """ + question;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    public String tradeFeedback(Long tradeId) {
        TradeResponse trade = tradeService.getTrade(tradeId);
        String tradeSummary = describeTrade(trade);

        String userPrompt = tradeSummary + """


                Give brief, specific feedback on this single trade: was risk management sound
                (stop loss / take profit set relative to entry, risk:reward ratio), does the
                outcome match the plan, and what's one thing worth reflecting on before the next trade?
                Keep it under 150 words.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /** AI Trade Rating: grades a single trade A-F based on risk management and plan adherence. */
    public String rateTrade(Long tradeId) {
        TradeResponse trade = tradeService.getTrade(tradeId);
        String tradeSummary = describeTrade(trade);

        String userPrompt = tradeSummary + """


                Grade this single trade on execution quality — NOT on whether it made money,
                but on whether risk was managed well (stop loss and take profit set sensibly
                relative to entry, a reasonable risk:reward ratio, position sized thoughtfully).
                A trade that lost money but followed a sound risk plan can still earn a good grade;
                a trade that won money by accident with no stop loss should NOT earn a good grade.

                Respond in exactly this format:
                Grade: <a single letter, A through F>
                Reason: <one or two sentences explaining the grade, citing the specific numbers above>
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /**
     * AI Emotion Detection: scans the trader's own written notes across their trade history
     * for recognizable trading-psychology patterns (FOMO, revenge trading, hesitation, etc).
     * This looks only at behavioral labels common in trading psychology, not at the person's
     * mental health — it is not a psychological or medical assessment of the user.
     */
    public String emotionAnalysis() {
        List<TradeResponse> trades = tradeService.getAllTrades();

        String notesBlock = trades.stream()
                .filter(t -> t.getNotes() != null && !t.getNotes().isBlank())
                .limit(MAX_TRADES_IN_CONTEXT)
                .map(t -> String.format("- [%s, %s] \"%s\"",
                        t.getSymbol(),
                        t.getPnl() == null ? "open" : (t.getPnl().signum() > 0 ? "win" : "loss"),
                        t.getNotes()))
                .collect(Collectors.joining("\n"));

        if (notesBlock.isBlank()) {
            return "Not enough journal notes yet to detect behavioral patterns. Try writing a short " +
                    "note on your reasoning and mindset each time you log a trade, then check back here.";
        }

        String userPrompt = """
                Here are notes the trader wrote on their own past trades, each tagged with whether
                that trade was a win or loss:

                """ + notesBlock + """


                Identify recurring trading-psychology patterns visible in these notes — for example
                FOMO, revenge trading after a loss, hesitation/second-guessing, overconfidence after
                wins, ignoring their own plan, or good discipline worth reinforcing. Only report
                patterns you can actually point to in the notes above; don't invent ones that aren't
                there. For each pattern found, quote or paraphrase the note that shows it. Keep it
                under 250 words and end with one practical suggestion.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /** AI Weekly Report: narrative summary of the last 7 days of closed trades. */
    public String weeklyReport() {
        List<Trade> weekTrades = analyticsService.getLastSevenDaysClosedTrades();

        if (weekTrades.isEmpty()) {
            return "No closed trades in the last 7 days, so there's nothing to report on yet. " +
                    "Log a few trades this week and check back for your first weekly report.";
        }

        String lines = weekTrades.stream()
                .map(t -> {
                    var pnl = TradeService.calculatePnl(t);
                    return String.format("- %s | %s | %s | entry=%s exit=%s | P/L=%s | strategy=%s",
                            t.getExitDate(), t.getSymbol(), t.getTradeType(),
                            t.getEntryPrice(), t.getExitPrice(),
                            pnl == null ? "n/a" : pnl,
                            t.getStrategy() == null || t.getStrategy().isBlank() ? "none" : t.getStrategy());
                })
                .collect(Collectors.joining("\n"));

        String userPrompt = """
                Here are the trader's closed trades from the last 7 days:

                """ + lines + """


                Write a short weekly performance report: overall result for the week, what went well,
                what didn't, and one focus area for next week. Keep it under 200 words, written like a
                coach's weekly check-in, not a generic summary.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /**
     * AI Journal Summary: distinct from getInsights() — this looks ONLY at the qualitative
     * notes the trader wrote (lessons, reasoning, mistakes), not the performance numbers,
     * and surfaces recurring themes across the whole journal.
     */
    public String journalSummary() {
        List<TradeResponse> trades = tradeService.getAllTrades();

        String notesBlock = trades.stream()
                .filter(t -> t.getNotes() != null && !t.getNotes().isBlank())
                .map(t -> "- \"" + t.getNotes() + "\"")
                .collect(Collectors.joining("\n"));

        if (notesBlock.isBlank()) {
            return "No journal notes written yet. Add a short note to your trades — your reasoning, " +
                    "what you were thinking, what you'd do differently — and this summary will surface " +
                    "recurring themes across your journal over time.";
        }

        String userPrompt = """
                Here are every note the trader has written across their trade journal:

                """ + notesBlock + """


                Summarize the recurring THEMES across these journal notes only (not the P/L numbers) —
                lessons that keep coming up, mistakes mentioned more than once, and any advice the
                trader has clearly already given themselves. Group into short bullet themes.
                Keep it under 200 words.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /** AI Strategy Analyzer: narrates the deterministic per-strategy stats from AnalyticsService. */
    public String strategyAnalysisNarrative() {
        var breakdown = analyticsService.getStrategyBreakdown();

        if (breakdown.getStats().isEmpty()) {
            return "No closed trades yet to break down by strategy. Tag your trades with a strategy " +
                    "name when you log them (e.g. \"Breakout\", \"Scalping\") to unlock this analysis.";
        }

        String table = breakdown.getStats().stream()
                .map(s -> String.format(
                        "- %s: %d trades, %.1f%% win rate, total P/L=%s, avg P/L=%s, avg R:R=1:%.2f",
                        s.getStrategy(), s.getTotalTrades(), s.getWinRatePercent(),
                        s.getTotalPnl(), s.getAveragePnl(), s.getAverageRiskRewardRatio()))
                .collect(Collectors.joining("\n"));

        String userPrompt = """
                Here is the trader's performance broken down by strategy tag:

                """ + table + """


                Based only on these numbers, which strategy is actually working, which is dragging
                down performance, and is there a strategy with too few trades to draw conclusions
                from yet? Give one concrete recommendation. Keep it under 180 words.
                """;

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /**
     * AI Trade Outlook (deliberately NOT "prediction"): narrates how the trader's own past
     * trades with a similar symbol/strategy setup have performed. Grounded strictly in their
     * own historical stats — never phrased as a forecast of what will happen next.
     */
    public String tradeOutlookNarrative(TradeOutlookResponse stats) {
        if (stats.getMatchingTradeCount() == 0) {
            return "No past trades match this symbol/strategy combination yet, so there's no " +
                    "historical pattern to reflect on. This will become useful once you've logged " +
                    "a few trades with this setup.";
        }

        String userPrompt = """
                The trader is about to consider a setup with symbol "%s" and strategy "%s".
                Here is how their OWN past trades matching this exact combination performed:

                - Matching trades: %d
                - Win rate: %.1f%%
                - Average P/L per trade: %s
                - Average risk:reward ratio: 1:%.2f

                Write a short, grounded reflection (2-4 sentences) on what this history suggests
                about this specific setup for THIS trader. Do not predict what will happen this time —
                make clear this is a look backward at their own track record, not a forecast. If the
                sample size is small (fewer than 5 trades), say so explicitly and caution against
                drawing strong conclusions.
                """.formatted(
                stats.getSymbol(), stats.getStrategy(),
                stats.getMatchingTradeCount(), stats.getWinRatePercent(),
                stats.getAveragePnl(), stats.getAverageRiskRewardRatio());

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    /** AI Risk Calculator commentary: a short qualitative note layered on top of the deterministic calculation. */
    public String riskTip(RiskCalcRequest request, RiskCalcResponse result) {
        String userPrompt = """
                The trader is planning a position with these risk parameters:
                - Account balance: %s
                - Risk per trade: %s%%
                - Entry price: %s | Stop loss: %s%s
                - Calculated risk amount: %s
                - Suggested position size: %s units (position value: %s)
                %s

                In 2-3 sentences, comment on whether this risk setup looks sound (e.g. is the risk
                percentage within a commonly used 1-2%% range, is the position size reasonable relative
                to account size). Do not comment on whether the trade itself is a good idea — only on
                the risk sizing math.
                """.formatted(
                request.getAccountBalance(), request.getRiskPercentage(),
                request.getEntryPrice(), request.getStopLossPrice(),
                request.getTakeProfitPrice() != null ? (" | Take profit: " + request.getTakeProfitPrice()) : "",
                result.getRiskAmount(), result.getSuggestedQuantity(), result.getPositionValue(),
                result.getRiskRewardRatio() != null ? ("- Resulting risk:reward ratio: 1:" + result.getRiskRewardRatio()) : ""
        );

        return aiClient.chat(COACH_SYSTEM_PROMPT, userPrompt);
    }

    private String describeTrade(TradeResponse trade) {
        return """
                Trade to review:
                - Symbol: %s
                - Direction: %s
                - Status: %s
                - Entry price: %s | Exit price: %s | Quantity: %s
                - Stop loss: %s | Take profit: %s
                - Risk:Reward ratio: %s
                - Realized P/L: %s
                - Strategy tag: %s
                - Trader's notes: "%s"
                """.formatted(
                trade.getSymbol(),
                trade.getTradeType(),
                trade.getStatus(),
                trade.getEntryPrice(),
                trade.getExitPrice() == null ? "n/a (still open)" : trade.getExitPrice(),
                trade.getQuantity(),
                trade.getStopLoss() == null ? "not set" : trade.getStopLoss(),
                trade.getTakeProfit() == null ? "not set" : trade.getTakeProfit(),
                trade.getRiskRewardRatio() == null ? "not set" : ("1:" + trade.getRiskRewardRatio()),
                trade.getPnl() == null ? "n/a" : trade.getPnl(),
                trade.getStrategy() == null || trade.getStrategy().isBlank() ? "none" : trade.getStrategy(),
                trade.getNotes() == null || trade.getNotes().isBlank() ? "(no notes written)" : trade.getNotes()
        );
    }

    private String buildContext(List<TradeResponse> trades, AnalyticsResponse analytics) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== OVERALL PERFORMANCE STATS ===\n");
        sb.append("Total trades: ").append(analytics.getTotalTrades()).append("\n");
        sb.append("Closed trades: ").append(analytics.getClosedTrades()).append("\n");
        sb.append("Win rate: ").append(analytics.getWinRatePercent()).append("%\n");
        sb.append("Total P/L: ").append(analytics.getTotalPnl()).append("\n");
        sb.append("Average win: ").append(analytics.getAverageWin()).append("\n");
        sb.append("Average loss: ").append(analytics.getAverageLoss()).append("\n");
        sb.append("Profit factor: ").append(analytics.getProfitFactor()).append("\n");
        sb.append("Expectancy per trade: ").append(analytics.getExpectancy()).append("\n");
        sb.append("Average risk:reward ratio: 1:").append(analytics.getAverageRiskRewardRatio()).append("\n");
        sb.append("Max drawdown: ").append(analytics.getMaxDrawdown()).append("\n");
        sb.append("Current streak (positive = win streak, negative = loss streak): ")
                .append(analytics.getCurrentStreak()).append("\n\n");

        sb.append("=== RECENT TRADES (most recent first, up to ")
                .append(MAX_TRADES_IN_CONTEXT).append(") ===\n");

        String tradeLines = trades.stream()
                .limit(MAX_TRADES_IN_CONTEXT)
                .map(t -> String.format(
                        "- %s | %s | %s | entry=%s exit=%s qty=%s | R:R=%s | P/L=%s | strategy=%s | notes=\"%s\"",
                        t.getEntryDate(),
                        t.getSymbol(),
                        t.getTradeType(),
                        t.getEntryPrice(),
                        t.getExitPrice() == null ? "open" : t.getExitPrice(),
                        t.getQuantity(),
                        t.getRiskRewardRatio() == null ? "n/a" : t.getRiskRewardRatio(),
                        t.getPnl() == null ? "n/a" : t.getPnl(),
                        t.getStrategy() == null || t.getStrategy().isBlank() ? "none" : t.getStrategy(),
                        t.getNotes() == null || t.getNotes().isBlank() ? "" : t.getNotes()
                ))
                .collect(Collectors.joining("\n"));

        sb.append(tradeLines.isBlank() ? "(no trades logged yet)" : tradeLines);

        return sb.toString();
    }
}
