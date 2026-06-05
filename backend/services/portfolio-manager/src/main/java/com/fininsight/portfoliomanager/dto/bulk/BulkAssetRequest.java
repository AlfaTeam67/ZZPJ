package com.fininsight.portfoliomanager.dto.bulk;

import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkAssetRequest(
    @NotEmpty @Valid List<AddAssetRequest> assets
) {}
