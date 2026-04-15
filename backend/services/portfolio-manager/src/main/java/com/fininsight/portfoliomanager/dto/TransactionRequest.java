package com.fininsight.portfoliomanager.dto;

import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private UUID assetId;

    @NotNull
    private TransactionType type;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String currency;

    private BigDecimal fee;
    private Instant executedAt;
    private String notes;
}
