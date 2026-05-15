package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.client.MarketDataClient;
import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.dto.valuation.AssetValuationDto;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationResponse;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValuationServiceTest {

    private static final String TOKEN = "test-jwt-token";

    @Mock
    private PortfolioDataRepository portfolioRepository;

    @Mock
    private MarketDataClient marketDataClient;

    @InjectMocks
    private ValuationService valuationService;

    @Test
    void shouldReturnFullValuationWhenAllPricesAvailable() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, userId, List.of(
            asset("AAPL", AssetType.STOCK, new BigDecimal("10"), new BigDecimal("150.00")),
            asset("BTC",  AssetType.CRYPTO, new BigDecimal("0.5"), new BigDecimal("40000.00"))
        ));

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(marketDataClient.getPrice("AAPL", TOKEN)).thenReturn(new BigDecimal("180.00"));
        when(marketDataClient.getPrice("BTC",  TOKEN)).thenReturn(new BigDecimal("50000.00"));

        var result = valuationService.valuate(portfolioId, userId, TOKEN);

        assertThat(result.portfolioId()).isEqualTo(portfolioId);
        assertThat(result.assets()).hasSize(2);
        assertThat(result.totalValue()).isEqualByComparingTo("26800.00");

        var aapl = findAsset(result, "AAPL");
        assertThat(aapl.currentPrice()).isEqualByComparingTo("180.00");
        assertThat(aapl.currentValue()).isEqualByComparingTo("1800.00");
        assertThat(aapl.gainLoss()).isEqualByComparingTo("300.00");
        assertThat(aapl.gainLossPct()).isEqualByComparingTo("20.0000");

        var btc = findAsset(result, "BTC");
        assertThat(btc.currentPrice()).isEqualByComparingTo("50000.00");
        assertThat(btc.currentValue()).isEqualByComparingTo("25000.00");
        assertThat(btc.gainLoss()).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldReturnNullFieldsForUnavailableMarketPrice() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, userId, List.of(
            asset("AAPL", AssetType.STOCK, new BigDecimal("10"), new BigDecimal("150.00"))
        ));

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(marketDataClient.getPrice("AAPL", TOKEN)).thenReturn(null);

        var result = valuationService.valuate(portfolioId, userId, TOKEN);

        assertThat(result.totalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        var aapl = findAsset(result, "AAPL");
        assertThat(aapl.currentPrice()).isNull();
        assertThat(aapl.currentValue()).isNull();
        assertThat(aapl.gainLoss()).isNull();
    }

    @Test
    void shouldReturnZeroTotalForEmptyPortfolio() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, userId, List.of());

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));

        PortfolioValuationResponse result = valuationService.valuate(portfolioId, userId, TOKEN);

        assertThat(result.totalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.assets()).isEmpty();
    }

    @Test
    void shouldThrowNotFoundWhenPortfolioMissing() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> valuationService.valuate(portfolioId, userId, TOKEN))
            .isInstanceOf(PortfolioNotFoundException.class)
            .hasMessageContaining("Portfolio not found");
    }

    @Test
    void shouldReturnNegativeGainLossWhenPriceFalls() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, userId, List.of(
            asset("AAPL", AssetType.STOCK, new BigDecimal("10"), new BigDecimal("200.00"))
        ));

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(marketDataClient.getPrice("AAPL", TOKEN)).thenReturn(new BigDecimal("150.00"));

        var result = valuationService.valuate(portfolioId, userId, TOKEN);

        var aapl = findAsset(result, "AAPL");
        assertThat(aapl.gainLoss()).isEqualByComparingTo("-500.00");
        assertThat(aapl.gainLossPct()).isNegative();
        assertThat(result.totalValue()).isEqualByComparingTo("1500.00");
    }

    @Test
    void shouldOnlyIncludePricedAssetsInTotalValue() {
        var portfolioId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, userId, List.of(
            asset("AAPL", AssetType.STOCK, new BigDecimal("10"), new BigDecimal("150.00")),
            asset("XYZ",  AssetType.STOCK, new BigDecimal("5"),  new BigDecimal("100.00"))
        ));

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(marketDataClient.getPrice("AAPL", TOKEN)).thenReturn(new BigDecimal("200.00"));
        when(marketDataClient.getPrice("XYZ",  TOKEN)).thenReturn(null);

        var result = valuationService.valuate(portfolioId, userId, TOKEN);

        assertThat(result.totalValue()).isEqualByComparingTo("2000.00");
        assertThat(findAsset(result, "XYZ").currentValue()).isNull();
    }

    @Test
    void shouldThrowAccessDeniedForOtherUsersPortfolio() {
        var portfolioId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var attackerId = UUID.randomUUID();
        var portfolio = portfolioWithAssets(portfolioId, ownerId, List.of());

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> valuationService.valuate(portfolioId, attackerId, TOKEN))
            .isInstanceOf(PortfolioAccessDeniedException.class);
    }

    private static AssetValuationDto findAsset(PortfolioValuationResponse response, String symbol) {
        return response.assets().stream()
            .filter(a -> a.symbol().equals(symbol))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Asset " + symbol + " not found in response"));
    }

    private static Portfolio portfolioWithAssets(UUID portfolioId, UUID userId, List<Asset> assets) {
        var user = new User();
        user.setId(userId);

        var portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setName("Test Portfolio");
        portfolio.setUser(user);
        assets.forEach(a -> {
            a.setPortfolio(portfolio);
            portfolio.getAssets().add(a);
        });
        return portfolio;
    }

    private static Asset asset(String symbol, AssetType type, BigDecimal quantity, BigDecimal avgBuyPrice) {
        var asset = new Asset();
        asset.setSymbol(symbol);
        asset.setType(type);
        asset.setQuantity(quantity);
        asset.setAvgBuyPrice(avgBuyPrice);
        asset.setCurrency("USD");
        return asset;
    }
}
