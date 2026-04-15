package pl.alfateam.portfoliomanager.domain;

import java.math.BigDecimal;

public record BondAsset(String symbol, BigDecimal quantity, BigDecimal avgBuyPrice) implements AssetRecord {
}
