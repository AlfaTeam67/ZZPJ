package com.fininsight.advisor.client;

import com.fininsight.advisor.dto.external.PortfolioValuationDto;
import com.fininsight.advisor.exception.PortfolioNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Klient portfolio-manager. Propaguje JWT użytkownika w nagłówku Authorization,
 * dzięki czemu portfolio-manager weryfikuje uprawnienia po stronie source of truth.
 */
@Slf4j
@Component
public class PortfolioClient {

    private final RestClient restClient;

    public PortfolioClient(@LoadBalanced RestClient.Builder builder) {
        this.restClient = builder
            .baseUrl("http://portfolio-manager")
            .build();
    }

    public PortfolioValuationDto getValuation(UUID portfolioId, String bearerToken) {
        try {
            return restClient.get()
                .uri("/api/portfolios/{id}/valuation", portfolioId)
                .header("Authorization", "Bearer " + bearerToken)
                .retrieve()
                .body(PortfolioValuationDto.class);
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 403 || status.value() == 404) {
                // Propaguj błąd uprawnień / brak portfela jako 4xx do frontu, nie maskuj jako 502.
                throw new ResponseStatusException(status, "Portfolio access denied or not found");
            }
            log.warn("portfolio-manager returned {} for portfolio {}: {}", status, portfolioId, e.getMessage());
            throw new PortfolioNotAvailableException("portfolio-manager error " + status, e);
        } catch (Exception e) {
            log.warn("portfolio-manager unreachable for portfolio {}: {}", portfolioId, e.getMessage());
            throw new PortfolioNotAvailableException("portfolio-manager unreachable", e);
        }
    }
}
