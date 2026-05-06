package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.mapper.TransactionMapper;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.AssetNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int PRICE_SCALE = 4;

    private final PortfolioDataRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(UUID portfolioId, UUID userId, TransactionRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        Instant now = Instant.now();
        Instant executedAt = request.executedAt() != null ? request.executedAt() : now;
        if (executedAt.isAfter(now)) {
            throw new IllegalArgumentException("Transaction execution time cannot be in the future");
        }

        Asset asset = getOrCreateAsset(portfolio, request);

        updateAssetPosition(asset, request);
        assetRepository.save(asset);

        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setPortfolio(portfolio);
        transaction.setType(request.type());
        transaction.setQuantity(request.quantity());
        transaction.setPrice(request.price());
        transaction.setCurrency(request.currency());
        transaction.setFee(request.fee());
        transaction.setExecutedAt(executedAt);
        transaction.setNotes(request.notes());

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(savedTransaction);
    }

    private Asset getOrCreateAsset(Portfolio portfolio, TransactionRequest request) {
        if (request.assetId() != null) {
            Asset asset = assetRepository.findByPortfolioIdAndId(portfolio.getId(), request.assetId())
                .orElseThrow(() -> new AssetNotFoundException("Asset not found"));
            validateAssetCurrency(asset, request.currency());
            return asset;
        }

        if (request.type() != TransactionType.BUY) {
            throw new IllegalArgumentException("Asset ID is required for SELL transactions");
        }

        if (request.symbol() == null || request.assetType() == null) {
            throw new IllegalArgumentException("Symbol and Asset Type are required when Asset ID is not provided");
        }

        Asset asset = assetRepository.findByPortfolioIdAndSymbol(portfolio.getId(), request.symbol())
            .orElseGet(() -> {
                Asset newAsset = new Asset();
                newAsset.setPortfolio(portfolio);
                newAsset.setSymbol(request.symbol());
                newAsset.setType(request.assetType());
                newAsset.setQuantity(BigDecimal.ZERO);
                newAsset.setAvgBuyPrice(BigDecimal.ZERO);
                newAsset.setCurrency(request.currency());
                return newAsset;
            });
        validateAssetCurrency(asset, request.currency());
        return asset;
    }

    private void updateAssetPosition(Asset asset, TransactionRequest request) {
        BigDecimal currentQuantity = asset.getQuantity();
        BigDecimal requestQuantity = request.quantity();
        BigDecimal requestPrice = request.price();
        BigDecimal requestFee = request.fee() != null ? request.fee() : BigDecimal.ZERO;

        if (requestFee.signum() < 0) {
            throw new IllegalArgumentException("Transaction fee cannot be negative");
        }

        if (request.type() == TransactionType.BUY) {
            BigDecimal existingCost = currentQuantity.multiply(asset.getAvgBuyPrice());
            BigDecimal newCost = requestQuantity.multiply(requestPrice).add(requestFee);
            BigDecimal updatedQuantity = currentQuantity.add(requestQuantity);
            BigDecimal updatedAvg = existingCost.add(newCost)
                .divide(updatedQuantity, PRICE_SCALE, RoundingMode.HALF_UP);
            asset.setQuantity(updatedQuantity);
            asset.setAvgBuyPrice(updatedAvg);
            return;
        }

        if (currentQuantity.compareTo(requestQuantity) < 0) {
            throw new IllegalArgumentException("Cannot sell more than owned quantity");
        }

        BigDecimal updatedQuantity = currentQuantity.subtract(requestQuantity);
        asset.setQuantity(updatedQuantity);
        if (updatedQuantity.signum() == 0) {
            asset.setAvgBuyPrice(BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP));
        }
    }

    private void validateAssetCurrency(Asset asset, String transactionCurrency) {
        if (!asset.getCurrency().equals(transactionCurrency)) {
            throw new IllegalArgumentException("Transaction currency must match asset currency");
        }
    }

}
