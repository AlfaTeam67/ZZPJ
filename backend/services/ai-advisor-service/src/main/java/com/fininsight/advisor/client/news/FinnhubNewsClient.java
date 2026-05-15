package com.fininsight.advisor.client.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.entity.enums.NewsSentiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Finnhub: GET https://finnhub.io/api/v1/company-news?symbol=AAPL&from=YYYY-MM-DD&to=YYYY-MM-DD
 * Zwraca listę newsów per ticker, działa darmowo dla giełdy USA.
 */
@Slf4j
@Component
public class FinnhubNewsClient {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestClient restClient;
    private final String apiKey;
    private final boolean enabled;

    public FinnhubNewsClient(
        @Qualifier("externalRestClientBuilder") RestClient.Builder builder,
        @Value("${app.finnhub.base-url:https://finnhub.io/api/v1}") String baseUrl,
        @Value("${app.finnhub.api-key:${FINNHUB_API_KEY:}}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<NewsItem> fetchCompanyNews(String symbol, LocalDate from, LocalDate to, int limit) {
        if (!enabled) {
            return Collections.emptyList();
        }
        try {
            List<FinnhubNewsDto> raw = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/company-news")
                    .queryParam("symbol", symbol)
                    .queryParam("from", from.format(ISO_DATE))
                    .queryParam("to", to.format(ISO_DATE))
                    .queryParam("token", apiKey)
                    .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

            if (raw == null) {
                return Collections.emptyList();
            }
            return raw.stream()
                .filter(n -> n.headline != null && !n.headline.isBlank())
                .limit(limit)
                .map(n -> new NewsItem(
                    n.id != null ? String.valueOf(n.id) : null,
                    n.headline,
                    n.source != null ? n.source : "Finnhub",
                    n.url,
                    symbol,
                    NewsProvider.FINNHUB,
                    NewsSentiment.NEUTRAL,
                    n.datetime != null ? Instant.ofEpochSecond(n.datetime) : Instant.now()
                ))
                .toList();
        } catch (RestClientResponseException e) {
            log.warn("Finnhub returned {} for symbol {}: {}", e.getStatusCode(), symbol, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Finnhub unreachable for symbol {}: {}", symbol, e.getMessage());
            return Collections.emptyList();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FinnhubNewsDto {
        public Long id;
        public String headline;
        public String source;
        public String url;
        public Long datetime;
        public String summary;
        public String related;
    }
}
