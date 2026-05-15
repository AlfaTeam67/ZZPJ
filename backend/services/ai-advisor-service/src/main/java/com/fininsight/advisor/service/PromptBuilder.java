package com.fininsight.advisor.service;

import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.dto.external.PortfolioValuationDto;
import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.enums.InvestmentHorizon;
import com.fininsight.advisor.entity.enums.RiskTolerance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class PromptBuilder {

    private static final String SYSTEM = """
        You are Fin-Insight, a financial assistant that produces concise, balanced investment commentary.
        You are not a licensed advisor. You must:
          * weigh portfolio composition against the user's risk tolerance and investment horizon,
          * cite which news headlines materially support each conclusion,
          * give 3-6 short, actionable bullet recommendations,
          * end with a single line "RISK_SCORE=<float 0..10>" reflecting overall portfolio risk vs the user's tolerance,
          * never recommend leverage, options, or derivatives unless the user is AGGRESSIVE.
        Use plain English. Do not invent prices or news that are not in the context.
        """;

    public List<LlmChatClient.ChatMessage> build(
        PortfolioValuationDto valuation,
        List<NewsCache> news,
        RiskTolerance risk,
        InvestmentHorizon horizon
    ) {
        StringBuilder user = new StringBuilder(2048);
        user.append("Risk tolerance: ").append(risk).append('\n');
        user.append("Investment horizon: ").append(horizon).append('\n');
        user.append("Portfolio total value: ").append(format(valuation.totalValue())).append('\n');
        user.append("Holdings:\n");

        if (valuation.assets() != null) {
            for (var a : valuation.assets()) {
                user.append("  - ")
                    .append(a.symbol() == null ? "?" : a.symbol())
                    .append(" (").append(a.type() == null ? "?" : a.type()).append(") ")
                    .append("qty=").append(format(a.quantity()))
                    .append(", avgBuy=").append(format(a.avgBuyPrice()))
                    .append(", current=").append(format(a.currentPrice()))
                    .append(", value=").append(format(a.currentValue()))
                    .append(", pnl%=").append(format(a.gainLossPct()))
                    .append('\n');
            }
        }

        user.append("\nRecent news headlines:\n");
        if (news == null || news.isEmpty()) {
            user.append("  (no recent headlines available)\n");
        } else {
            int idx = 1;
            for (NewsCache n : news) {
                user.append("  ").append(idx++).append(". [")
                    .append(n.getProvider() == null ? "?" : n.getProvider()).append("] ")
                    .append("[").append(n.getSymbol() == null ? "MARKET" : n.getSymbol()).append("] ")
                    .append(n.getHeadline()).append(" — ").append(n.getSource()).append('\n');
            }
        }

        user.append("""

            Produce:
              1) A 1-2 sentence summary about whether the portfolio is positioned for upside or downside given the news.
              2) A bullet list of 3-6 concrete recommendations (rebalancing, hedging, or "hold" calls).
              3) The final line: RISK_SCORE=<float between 0 and 10>
            """);

        return List.of(LlmChatClient.ChatMessage.system(SYSTEM), LlmChatClient.ChatMessage.user(user.toString()));
    }

    /**
     * Format BigDecimal pod prompt LLM. Trzymamy do 8 cyfr po przecinku, żeby
     * pozycje crypto (ETH, BTC) nie traciły precyzji. Negatywny lub pomijalnie
     * mały scale (np. dla 1E+2) jest podbijany do 0, żeby setScale nie rzucił
     * ArithmeticException.
     */
    private String format(BigDecimal v) {
        if (v == null) return "n/a";
        int targetScale = Math.max(0, Math.min(8, v.scale()));
        return v.setScale(targetScale, RoundingMode.HALF_UP).toPlainString();
    }
}
