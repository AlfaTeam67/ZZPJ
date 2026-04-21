package com.fininsight.portfoliomanager.dto;

import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    private UUID assetId;

    private AssetType assetType;

    private String symbol;

    @NotNull
    private TransactionType type;

    @NotNull
    @Positive(message = "Transaction quantity must be positive")
    private BigDecimal quantity;

    @NotNull
    @PositiveOrZero(message = "Transaction price cannot be negative")
    private BigDecimal price;

    @NotBlank
    private String currency;

    @DecimalMin(value = "0.0", inclusive = true, message = "Transaction fee cannot be negative")
    private BigDecimal fee;
    private Instant executedAt;
    private String notes;
}
