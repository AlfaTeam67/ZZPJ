package com.fininsight.marketdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Scheduler and rate-limit tuning parameters for the market-data price refresh.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "market-data.scheduler")
public class MarketDataSchedulerProperties {

    /** Master switch — set to {@code false} to disable the scheduled refresh entirely. */
    private boolean enabled = true;

    /**
     * Cron expression for trading-hours refresh (Mon-Fri, market-hours only).
     * Default: every 5 minutes on weekdays between 09:00-23:00 UTC.
     */
    private String cron = "0 */5 9-23 * * MON-FRI";

    /**
     * Cron expression used for always-on symbols like crypto.
     * Default: every 10 minutes, 24/7.
     */
    private String cronAlways = "0 */10 * * * *";

    /**
     * Minimum pause between individual Finnhub calls to stay within the
     * free-tier rate limit (60 req/min → ~1 100 ms gap is safe).
     */
    private long interRequestDelayMs = 1100L;
}
