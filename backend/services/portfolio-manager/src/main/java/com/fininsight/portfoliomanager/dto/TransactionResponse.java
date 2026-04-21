package com.fininsight.portfoliomanager.dto;

import com.fininsight.portfoliomanager.domain.enums.TransactionType;
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
public class TransactionResponse {
    private UUID id;
    private UUID assetId;
    private UUID portfolioId;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private BigDecimal fee;
    private Instant executedAt;
    private String notes;
}
