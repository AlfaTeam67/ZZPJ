package com.fininsight.marketdata.cache;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceCacheEntry(
    String symbol,
    BigDecimal price,
    String currency,
    BigDecimal changePct24h,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    Instant fetchedAt
) {}
