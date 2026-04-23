package com.fininsight.portfoliomanager.dto.asset;

import com.fininsight.portfoliomanager.domain.enums.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AddAssetRequest(
    @NotNull AssetType type,
    @NotBlank @Size(max = 20) String symbol,
    @NotNull @DecimalMin("0.00000001") BigDecimal quantity,
    @NotNull @DecimalMin("0.01") BigDecimal avgBuyPrice,
    @NotBlank @Size(max = 10) String currency
) {}
