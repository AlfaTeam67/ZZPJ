package com.fininsight.portfoliomanager.dto.bulk;

import com.fininsight.portfoliomanager.dto.asset.AssetResponse;

import java.util.List;

public record BulkAssetResponse(
    List<AssetResponse> successful,
    List<BulkError> errors,
    int successCount,
    int errorCount
) {}
