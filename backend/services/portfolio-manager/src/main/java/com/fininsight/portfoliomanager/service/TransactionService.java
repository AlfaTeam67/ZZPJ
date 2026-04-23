package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.mapper.TransactionMapper;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public TransactionResponse createTransaction(UUID portfolioId, String userId, TransactionRequest request) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        Instant now = Instant.now();
        Instant executedAt = request.executedAt() != null ? request.executedAt() : now;
        if (executedAt.isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction execution time cannot be in the future");
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
            validateAssetCurrency(asset, request.currency());
            return asset;
        }

        if (request.type() != TransactionType.BUY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset ID is required for SELL transactions");
        }

        if (request.symbol() == null || request.assetType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol and Asset Type are required when Asset ID is not provided");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction fee cannot be negative");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell more than owned quantity");
        }

        BigDecimal updatedQuantity = currentQuantity.subtract(requestQuantity);
        asset.setQuantity(updatedQuantity);
        if (updatedQuantity.signum() == 0) {
            asset.setAvgBuyPrice(BigDecimal.ZERO.setScale(PRICE_SCALE, RoundingMode.HALF_UP));
        }
    }

    private void validateAssetCurrency(Asset asset, String transactionCurrency) {
        if (!asset.getCurrency().equals(transactionCurrency)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transaction currency must match asset currency"
            );
        }
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
