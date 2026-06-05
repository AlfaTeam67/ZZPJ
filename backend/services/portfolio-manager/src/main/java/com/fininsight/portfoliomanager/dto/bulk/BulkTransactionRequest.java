package com.fininsight.portfoliomanager.dto.bulk;

import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkTransactionRequest(
    @NotEmpty @Valid List<TransactionRequest> transactions
) {}
