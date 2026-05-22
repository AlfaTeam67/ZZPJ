package com.fininsight.advisor.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser odpowiedzi LLM. Wyciąga linie z bullet pointami oraz końcowe RISK_SCORE=...
 * Tolerancyjny - jeżeli model nie poda risk score, zwracamy null i nadrzędna logika
 * może doliczyć score deterministycznie.
 */
@Component
public class LlmResponseParser {

    private static final Pattern RISK_SCORE = Pattern.compile("RISK_SCORE\\s*=\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BULLET = Pattern.compile("^\\s*(?:[-*•]|\\d+[\\.)])\\s+(.+?)\\s*$");

    public ParsedRecommendation parse(String content) {
        if (content == null || content.isBlank()) {
            return new ParsedRecommendation("", List.of(), "", null);
        }
        String[] lines = content.split("\\R");

        List<String> bullets = new ArrayList<>();
        StringBuilder summary = new StringBuilder();
        BigDecimal score = null;
        boolean foundFirstBullet = false;

        for (String raw : lines) {
            Matcher rs = RISK_SCORE.matcher(raw);
            if (rs.find()) {
                try {
                    score = clamp(new BigDecimal(rs.group(1)));
                } catch (NumberFormatException ignored) {
                    // ignore malformed score
                }
                continue;
            }

            Matcher bm = BULLET.matcher(raw);
            if (bm.matches()) {
                bullets.add(bm.group(1).trim());
                foundFirstBullet = true;
                continue;
            }

            if (!foundFirstBullet && !raw.isBlank()) {
                if (summary.length() > 0) summary.append(' ');
                summary.append(raw.trim());
            }
        }

        String summaryText = summary.toString().trim();
        if (summaryText.length() > 500) {
            summaryText = summaryText.substring(0, 500);
        }
        return new ParsedRecommendation(summaryText, List.copyOf(bullets), content, score);
    }

    private BigDecimal clamp(BigDecimal score) {
        if (score.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        if (score.compareTo(new BigDecimal("10")) > 0) return new BigDecimal("10");
        return score.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public record ParsedRecommendation(String summary, List<String> bullets, String fullText, BigDecimal riskScore) {
        public Optional<BigDecimal> riskScoreOpt() { return Optional.ofNullable(riskScore); }
    }
}
