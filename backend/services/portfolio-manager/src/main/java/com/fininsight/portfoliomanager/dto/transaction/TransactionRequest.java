package com.fininsight.portfoliomanager.dto.transaction;

import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRequest(
    UUID assetId,
    @NotNull TransactionType type,
    @NotNull @DecimalMin("0.00000001") BigDecimal quantity,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotBlank @Size(max = 10) String currency,
    @DecimalMin("0") BigDecimal fee,
    Instant executedAt,
    String notes,
    // Fields for creating a new asset if assetId is null
    String symbol,
    AssetType assetType
) {}
