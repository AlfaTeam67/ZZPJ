package pl.alfateam.portfoliomanager.domain;

import java.math.BigDecimal;

public record CryptoAsset(String symbol, BigDecimal quantity, BigDecimal avgBuyPrice) implements AssetRecord {
}
