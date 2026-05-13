package com.fininsight.portfoliomanager.dto.valuation;

import com.fininsight.portfoliomanager.domain.enums.AssetType;

import java.math.BigDecimal;

public record AssetValuationDto(
    String symbol,
    AssetType type,
    BigDecimal quantity,
    BigDecimal avgBuyPrice,
    BigDecimal currentPrice,
    BigDecimal currentValue,
    BigDecimal gainLoss,
    BigDecimal gainLossPct
) {}
