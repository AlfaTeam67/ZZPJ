package com.fininsight.portfoliomanager.domain;

public sealed interface AssetRecord permits StockAsset, CryptoAsset, BondAsset {
}
