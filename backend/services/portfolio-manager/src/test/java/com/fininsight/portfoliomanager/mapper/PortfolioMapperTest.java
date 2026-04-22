package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioMapperTest {

    @Autowired
    private PortfolioMapper mapper;

    @Test
    void shouldMapPortfolioToPortfolioResponse() {
        // given
        UUID portfolioId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setName("My Portfolio");
        portfolio.setDescription("Test description");
        portfolio.setCreatedAt(Instant.now());

        Asset asset = new Asset();
        asset.setId(UUID.randomUUID());
        asset.setSymbol("BTC");
        asset.setQuantity(new BigDecimal("0.5"));
        asset.setAvgBuyPrice(new BigDecimal("50000"));
        asset.setCurrency("USD");
        portfolio.getAssets().add(asset);

        // when
        PortfolioResponse response = mapper.toResponse(portfolio);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(portfolio.getId());
        assertThat(response.name()).isEqualTo(portfolio.getName());
        assertThat(response.description()).isEqualTo(portfolio.getDescription());
        assertThat(response.createdAt()).isEqualTo(portfolio.getCreatedAt());
        assertThat(response.assets()).hasSize(1);
        assertThat(response.assets().get(0).symbol()).isEqualTo("BTC");
    }

    @Test
    void shouldMapPortfolioToPortfolioSummaryResponse() {
        // given
        UUID portfolioId = UUID.randomUUID();
        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setName("My Portfolio");
        portfolio.setDescription("Test description");
        portfolio.setCreatedAt(Instant.now());

        // when
        List<PortfolioSummaryResponse> responses = mapper.toSummaryList(List.of(portfolio));

        // then
        assertThat(responses).hasSize(1);
        PortfolioSummaryResponse response = responses.get(0);
        assertThat(response.id()).isEqualTo(portfolio.getId());
        assertThat(response.name()).isEqualTo(portfolio.getName());
        assertThat(response.description()).isEqualTo(portfolio.getDescription());
        assertThat(response.createdAt()).isEqualTo(portfolio.getCreatedAt());
    }
}
