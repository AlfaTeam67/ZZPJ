package com.fininsight.portfoliomanager.dto.bulk;

import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;

import java.util.List;

public record BulkTransactionResponse(
    List<TransactionResponse> successful,
    List<BulkError> errors,
    int successCount,
    int errorCount
) {}
