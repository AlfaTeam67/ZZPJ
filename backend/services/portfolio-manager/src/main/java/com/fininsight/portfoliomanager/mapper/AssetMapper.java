package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetResponse toResponse(Asset asset);
}
