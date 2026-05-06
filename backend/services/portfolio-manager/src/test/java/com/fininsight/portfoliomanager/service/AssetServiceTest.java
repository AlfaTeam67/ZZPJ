package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import com.fininsight.portfoliomanager.exception.AssetNotFoundException;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.mapper.AssetMapper;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private PortfolioDataRepository portfolioRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AssetMapper assetMapper;

    @InjectMocks
    private AssetService assetService;

    @Test
    void shouldUpdateExistingAssetAndCreateBuyTransaction() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID portfolioId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        Portfolio portfolio = portfolio(portfolioId, userId);
        Asset existingAsset = asset(portfolio, "AAPL", "2.00000000", "100.0000", "USD");
        AddAssetRequest request = new AddAssetRequest(AssetType.STOCK, "AAPL", new BigDecimal("1.00000000"), new BigDecimal("130.0000"), "USD");
        AssetResponse response = new AssetResponse(UUID.randomUUID(), AssetType.STOCK, "AAPL", new BigDecimal("3.00000000"), new BigDecimal("110.0000"), "USD", Instant.now());

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndSymbol(portfolioId, "AAPL")).thenReturn(Optional.of(existingAsset));
        when(assetMapper.toResponse(existingAsset)).thenReturn(response);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponse result = assetService.addAsset(portfolioId, request, userId);

        assertThat(result).isEqualTo(response);
        assertThat(existingAsset.getQuantity()).isEqualByComparingTo("3.00000000");
        assertThat(existingAsset.getAvgBuyPrice()).isEqualByComparingTo("110.0000");

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.BUY);
        assertThat(tx.getQuantity()).isEqualByComparingTo("1.00000000");
        assertThat(tx.getPrice()).isEqualByComparingTo("130.0000");
        assertThat(tx.getCurrency()).isEqualTo("USD");
    }

    @Test
    void shouldCreateNewAssetAndCreateBuyTransaction() {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID portfolioId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Portfolio portfolio = portfolio(portfolioId, userId);
        AddAssetRequest request = new AddAssetRequest(AssetType.CRYPTO, "BTC", new BigDecimal("0.50000000"), new BigDecimal("30000.0000"), "USD");

        Asset savedAsset = asset(portfolio, "BTC", "0.50000000", "30000.0000", "USD");
        savedAsset.setType(AssetType.CRYPTO);
        AssetResponse response = new AssetResponse(UUID.randomUUID(), AssetType.CRYPTO, "BTC", new BigDecimal("0.50000000"), new BigDecimal("30000.0000"), "USD", Instant.now());

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndSymbol(portfolioId, "BTC")).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenReturn(savedAsset);
        when(assetMapper.toResponse(savedAsset)).thenReturn(response);

        AssetResponse result = assetService.addAsset(portfolioId, request, userId);

        assertThat(result).isEqualTo(response);
        verify(assetRepository).save(any(Asset.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldRejectAddAssetForDifferentOwner() {
        UUID ownerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID requesterId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID portfolioId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        Portfolio portfolio = portfolio(portfolioId, ownerId);
        AddAssetRequest request = new AddAssetRequest(AssetType.STOCK, "AAPL", new BigDecimal("1.00000000"), new BigDecimal("100.0000"), "USD");

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> assetService.addAsset(portfolioId, request, requesterId))
            .isInstanceOf(PortfolioAccessDeniedException.class)
            .hasMessageContaining("Access denied");

        verify(assetRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldCreateSellTransactionAndDeleteAsset() {
        UUID userId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID portfolioId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        UUID assetId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

        Portfolio portfolio = portfolio(portfolioId, userId);
        Asset existingAsset = asset(portfolio, "AAPL", "2.00000000", "120.0000", "USD");
        existingAsset.setId(assetId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndId(portfolioId, assetId)).thenReturn(Optional.of(existingAsset));

        assetService.removeAsset(portfolioId, assetId, userId);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction tx = transactionCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.SELL);
        assertThat(tx.getQuantity()).isEqualByComparingTo("2.00000000");
        assertThat(tx.getPrice()).isEqualByComparingTo("120.0000");

        verify(assetRepository).delete(existingAsset);
    }

    @Test
    void shouldThrowWhenRemovingMissingAsset() {
        UUID userId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID portfolioId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        UUID assetId = UUID.fromString("10101010-1010-1010-1010-101010101010");

        Portfolio portfolio = portfolio(portfolioId, userId);
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findByPortfolioIdAndId(portfolioId, assetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.removeAsset(portfolioId, assetId, userId))
            .isInstanceOf(AssetNotFoundException.class)
            .hasMessageContaining("Asset not found");

        verify(assetRepository, never()).delete(any());
    }

    private static Portfolio portfolio(UUID portfolioId, UUID userId) {
        User user = new User();
        user.setId(userId);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setUser(user);
        return portfolio;
    }

    private static Asset asset(Portfolio portfolio, String symbol, String quantity, String avgBuyPrice, String currency) {
        Asset asset = new Asset();
        asset.setPortfolio(portfolio);
        asset.setType(AssetType.STOCK);
        asset.setSymbol(symbol);
        asset.setQuantity(new BigDecimal(quantity));
        asset.setAvgBuyPrice(new BigDecimal(avgBuyPrice));
        asset.setCurrency(currency);
        return asset;
    }
}
