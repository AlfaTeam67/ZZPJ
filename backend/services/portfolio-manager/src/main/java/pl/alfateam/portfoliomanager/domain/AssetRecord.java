package pl.alfateam.portfoliomanager.domain;

public sealed interface AssetRecord permits StockAsset, CryptoAsset, BondAsset {
}
