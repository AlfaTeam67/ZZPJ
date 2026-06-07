package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import com.fininsight.portfoliomanager.dto.bulk.BulkAssetRequest;
import com.fininsight.portfoliomanager.dto.bulk.BulkAssetResponse;
import com.fininsight.portfoliomanager.dto.bulk.BulkError;
import com.fininsight.portfoliomanager.dto.bulk.BulkTransactionRequest;
import com.fininsight.portfoliomanager.dto.bulk.BulkTransactionResponse;
import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bulk operations for assets and transactions with partial-success semantics.
 * Each item is processed independently — errors on one item do not roll back others.
 *
 * NOTE: Individual operations are delegated to existing AssetService / TransactionService,
 * which manage their own transactions. This service intentionally has no @Transactional
 * at class level to preserve per-item isolation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkOperationService {

    private final AssetService assetService;
    private final TransactionService transactionService;

    public BulkAssetResponse bulkAddAssets(UUID portfolioId, UUID userId, BulkAssetRequest request) {
        List<AssetResponse> successful = new ArrayList<>();
        List<BulkError> errors = new ArrayList<>();

        List<AddAssetRequest> items = request.assets();
        for (int i = 0; i < items.size(); i++) {
            try {
                AssetResponse response = assetService.addAsset(portfolioId, items.get(i), userId);
                successful.add(response);
            } catch (Exception e) {
                log.debug("Bulk asset error at index {}: {}", i, e.getMessage());
                errors.add(new BulkError(i, "asset", e.getMessage()));
            }
        }
        return new BulkAssetResponse(successful, errors, successful.size(), errors.size());
    }

    public BulkTransactionResponse bulkAddTransactions(
        UUID portfolioId, UUID userId, BulkTransactionRequest request
    ) {
        List<TransactionResponse> successful = new ArrayList<>();
        List<BulkError> errors = new ArrayList<>();

        List<TransactionRequest> items = request.transactions();
        for (int i = 0; i < items.size(); i++) {
            try {
                TransactionResponse response = transactionService.createTransaction(portfolioId, userId, items.get(i));
                successful.add(response);
            } catch (Exception e) {
                log.debug("Bulk transaction error at index {}: {}", i, e.getMessage());
                errors.add(new BulkError(i, "transaction", e.getMessage()));
            }
        }
        return new BulkTransactionResponse(successful, errors, successful.size(), errors.size());
    }
}
