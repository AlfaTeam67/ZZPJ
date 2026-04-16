package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.TransactionRequest;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private PortfolioDataRepository portfolioRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldUpdateAssetOnBuyTransaction() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID portfolioId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID assetId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Portfolio portfolio = portfolio(portfolioId, userId);
        Asset asset = asset(assetId, portfolio, "10.00000000", "100.0000");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndId(portfolioId, assetId)).thenReturn(Optional.of(asset));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
            return tx;
        });

        TransactionRequest request = TransactionRequest.builder()
            .assetId(assetId)
            .type(TransactionType.BUY)
            .quantity(new BigDecimal("5.00000000"))
            .price(new BigDecimal("200.0000"))
            .currency("USD")
            .build();

        transactionService.createTransaction(portfolioId, userId.toString(), request);

        assertThat(asset.getQuantity()).isEqualByComparingTo("15.00000000");
        assertThat(asset.getAvgBuyPrice()).isEqualByComparingTo("133.3333");
        verify(assetRepository).save(asset);
    }

    @Test
    void shouldIncludeFeeInAverageBuyPriceCalculation() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID portfolioId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID assetId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Portfolio portfolio = portfolio(portfolioId, userId);
        Asset asset = asset(assetId, portfolio, "10.00000000", "100.0000");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndId(portfolioId, assetId)).thenReturn(Optional.of(asset));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = TransactionRequest.builder()
            .assetId(assetId)
            .type(TransactionType.BUY)
            .quantity(new BigDecimal("5.00000000"))
            .price(new BigDecimal("200.0000"))
            .fee(new BigDecimal("15.0000"))
            .currency("USD")
            .build();

        transactionService.createTransaction(portfolioId, userId.toString(), request);

        assertThat(asset.getQuantity()).isEqualByComparingTo("15.00000000");
        assertThat(asset.getAvgBuyPrice()).isEqualByComparingTo("134.3333");
    }

    @Test
    void shouldRejectSellWhenQuantityIsTooHigh() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID portfolioId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID assetId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Portfolio portfolio = portfolio(portfolioId, userId);
        Asset asset = asset(assetId, portfolio, "2.00000000", "100.0000");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndId(portfolioId, assetId)).thenReturn(Optional.of(asset));

        TransactionRequest request = TransactionRequest.builder()
            .assetId(assetId)
            .type(TransactionType.SELL)
            .quantity(new BigDecimal("3.00000000"))
            .price(new BigDecimal("120.0000"))
            .currency("USD")
            .build();

        assertThatThrownBy(() -> transactionService.createTransaction(portfolioId, userId.toString(), request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Cannot sell more than owned quantity");
    }

    private static Portfolio portfolio(UUID portfolioId, UUID userId) {
        User user = new User();
        user.setId(userId);
        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setUser(user);
        return portfolio;
    }

    private static Asset asset(UUID assetId, Portfolio portfolio, String quantity, String avgBuyPrice) {
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setPortfolio(portfolio);
        asset.setQuantity(new BigDecimal(quantity));
        asset.setAvgBuyPrice(new BigDecimal(avgBuyPrice));
        asset.setCurrency("USD");
        return asset;
    }
}
