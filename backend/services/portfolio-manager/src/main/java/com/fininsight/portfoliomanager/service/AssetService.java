package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import com.fininsight.portfoliomanager.exception.AssetNotFoundException;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.mapper.AssetMapper;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private static final int PRICE_SCALE = 4;

    private final PortfolioDataRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final AssetMapper assetMapper;

    @Transactional
    public AssetResponse addAsset(UUID portfolioId, AddAssetRequest request, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        Optional<Asset> existingAssetOpt = assetRepository.findByPortfolioIdAndSymbol(portfolioId, request.symbol());

        Asset asset = existingAssetOpt.map(existingAsset -> {
            if (!existingAsset.getCurrency().equals(request.currency())) {
                throw new IllegalArgumentException(
                    "Cannot add to asset with a different currency. Existing: "
                    + existingAsset.getCurrency() + ", New: " + request.currency()
                );
            }
            updateAverageBuyPrice(existingAsset, request.quantity(), request.avgBuyPrice());
            return existingAsset;
        }).orElseGet(() -> assetRepository.save(createNewAsset(portfolio, request)));

        createBuyTransaction(asset, request);

        return assetMapper.toResponse(asset);
    }

    @Transactional
    public void removeAsset(UUID portfolioId, UUID assetId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        Asset asset = assetRepository.findByPortfolioIdAndId(portfolioId, assetId)
            .orElseThrow(() -> new AssetNotFoundException("Asset not found"));

        createSellTransaction(asset);

        assetRepository.delete(asset);
    }

    private Asset createNewAsset(Portfolio portfolio, AddAssetRequest request) {
        Asset asset = new Asset();
        asset.setPortfolio(portfolio);
        asset.setType(request.type());
        asset.setSymbol(request.symbol());
        asset.setQuantity(request.quantity());
        asset.setAvgBuyPrice(request.avgBuyPrice());
        asset.setCurrency(request.currency());
        return asset;
    }

    private void updateAverageBuyPrice(Asset asset, BigDecimal newQuantity, BigDecimal newPrice) {
        BigDecimal currentQuantity = asset.getQuantity();
        BigDecimal currentAvgPrice = asset.getAvgBuyPrice();
        
        BigDecimal totalCost = currentQuantity.multiply(currentAvgPrice).add(newQuantity.multiply(newPrice));
        BigDecimal totalQuantity = currentQuantity.add(newQuantity);
        
        BigDecimal newAvgPrice = totalCost.divide(totalQuantity, PRICE_SCALE, RoundingMode.HALF_UP);
        
        asset.setQuantity(totalQuantity);
        asset.setAvgBuyPrice(newAvgPrice);
    }

    private void createBuyTransaction(Asset asset, AddAssetRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setPortfolio(asset.getPortfolio());
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(request.quantity());
        transaction.setPrice(request.avgBuyPrice());
        transaction.setCurrency(request.currency());
        transaction.setFee(null);
        transaction.setExecutedAt(Instant.now());
        transaction.setNotes("Asset added via AssetService");
        
        transactionRepository.save(transaction);
    }

    private void createSellTransaction(Asset asset) {
        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setPortfolio(asset.getPortfolio());
        transaction.setType(TransactionType.SELL);
        transaction.setQuantity(asset.getQuantity());
        transaction.setPrice(asset.getAvgBuyPrice());
        transaction.setCurrency(asset.getCurrency());
        transaction.setExecutedAt(Instant.now());
        transaction.setNotes("Asset removed via AssetService");
        
        transactionRepository.save(transaction);
    }
}
