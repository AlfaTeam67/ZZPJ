package com.fininsight.portfoliomanager.domain;

import java.math.BigDecimal;

public record StockAsset(String symbol, BigDecimal quantity, BigDecimal avgBuyPrice) implements AssetRecord {
}
