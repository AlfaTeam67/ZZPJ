package com.fininsight.portfoliomanager.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class MarketDataClient {

    private final RestClient restClient;

    public MarketDataClient(@LoadBalanced RestClient.Builder builder) {
        this.restClient = builder
            .baseUrl("http://market-data-service")
            .build();
    }

    private record MarketPriceSnapshot(BigDecimal price, String currency) {}

    public BigDecimal getPrice(String symbol, String bearerToken) {
        try {
            var snapshots = restClient.get()
                .uri("/api/market-prices/symbol/{ticker}", symbol)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<MarketPriceSnapshot>>() {});

            if (snapshots == null || snapshots.isEmpty()) {
                return null;
            }
            return snapshots.getFirst().price();
        } catch (RestClientResponseException e) {
            log.warn("Market data service returned {} for symbol {}: {}", e.getStatusCode(), symbol, e.getMessage());
            return null;
        } catch (RestClientException e) {
            log.warn("Market data service unreachable for symbol {}: {}", symbol, e.getMessage());
            return null;
        }
    }
}
