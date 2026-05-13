package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.client.MarketDataClient;
import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.dto.valuation.AssetValuationDto;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationResponse;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class ValuationService {

    private final PortfolioDataRepository portfolioRepository;
    private final MarketDataClient marketDataClient;

    @Transactional(readOnly = true)
    public PortfolioValuationResponse valuate(UUID portfolioId, UUID userId, String bearerToken) {
        var portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        var assetValuations = valuateAssetsInParallel(portfolio.getAssets(), bearerToken);

        var totalValue = assetValuations.stream()
            .map(AssetValuationDto::currentValue)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioValuationResponse(portfolioId, totalValue, assetValuations, Instant.now());
    }

    private List<AssetValuationDto> valuateAssetsInParallel(List<Asset> assets, String bearerToken) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<AssetValuationDto>> futures = assets.stream()
                .map(asset -> executor.submit(() -> valuateAsset(asset, bearerToken)))
                .toList();

            return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Valuation interrupted", e);
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof RuntimeException rte) {
                            throw rte;
                        }
                        throw new RuntimeException("Valuation failed", e.getCause());
                    }
                })
                .toList();
        }
    }

    private AssetValuationDto valuateAsset(Asset asset, String bearerToken) {
        var currentPrice = marketDataClient.getPrice(asset.getSymbol(), bearerToken);

        if (currentPrice == null) {
            return new AssetValuationDto(
                asset.getSymbol(), asset.getType(), asset.getQuantity(), asset.getAvgBuyPrice(),
                null, null, null, null
            );
        }

        var currentValue = currentPrice.multiply(asset.getQuantity());
        var cost = asset.getAvgBuyPrice().multiply(asset.getQuantity());
        var gainLoss = currentValue.subtract(cost);
        var gainLossPct = cost.compareTo(BigDecimal.ZERO) != 0
            ? gainLoss.divide(cost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return new AssetValuationDto(
            asset.getSymbol(), asset.getType(), asset.getQuantity(), asset.getAvgBuyPrice(),
            currentPrice, currentValue, gainLoss, gainLossPct
        );
    }
}
