package com.fininsight.marketdata.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Deserialisation target for Finnhub's {@code GET /quote} endpoint.
 *
 * <p>Field names follow Finnhub's documented JSON keys:</p>
 * <ul>
 *   <li>{@code c}  — current price</li>
 *   <li>{@code d}  — change</li>
 *   <li>{@code dp} — percent change</li>
 *   <li>{@code h}  — high price of the day</li>
 *   <li>{@code l}  — low price of the day</li>
 *   <li>{@code o}  — open price of the day</li>
 *   <li>{@code pc} — previous close price</li>
 *   <li>{@code t}  — timestamp (Unix epoch seconds)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class FinnhubQuoteResponse {

    /** Current price. */
    @JsonProperty("c")
    private BigDecimal currentPrice;

    /** Change since previous close. */
    @JsonProperty("d")
    private BigDecimal change;

    /** Percent change since previous close. */
    @JsonProperty("dp")
    private BigDecimal percentChange;

    /** Day high. */
    @JsonProperty("h")
    private BigDecimal dayHigh;

    /** Day low. */
    @JsonProperty("l")
    private BigDecimal dayLow;

    /** Day open. */
    @JsonProperty("o")
    private BigDecimal dayOpen;

    /** Previous close. */
    @JsonProperty("pc")
    private BigDecimal previousClose;

    /** Unix epoch seconds of the quote. */
    @JsonProperty("t")
    private Long timestamp;

    /**
     * Returns {@code true} when Finnhub provides no data for the symbol.
     * Finnhub returns {@code {"c":0,"d":null,"dp":null,...}} for unknown symbols.
     */
    public boolean isEmpty() {
        return currentPrice == null || BigDecimal.ZERO.compareTo(currentPrice) == 0;
    }
}
