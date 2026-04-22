package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AssetMapperTest {

    private final AssetMapper mapper = Mappers.getMapper(AssetMapper.class);

    @Test
    void shouldMapAssetToAssetResponse() {
        // given
        UUID assetId = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setType(AssetType.STOCK);
        asset.setSymbol("AAPL");
        asset.setQuantity(new BigDecimal("10.5"));
        asset.setAvgBuyPrice(new BigDecimal("150.25"));
        asset.setCurrency("USD");
        asset.setAddedAt(Instant.now());

        // when
        AssetResponse response = mapper.toResponse(asset);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(asset.getId());
        assertThat(response.type()).isEqualTo(asset.getType());
        assertThat(response.symbol()).isEqualTo(asset.getSymbol());
        assertThat(response.quantity()).isEqualByComparingTo(asset.getQuantity());
        assertThat(response.avgBuyPrice()).isEqualByComparingTo(asset.getAvgBuyPrice());
        assertThat(response.currency()).isEqualTo(asset.getCurrency());
        assertThat(response.addedAt()).isEqualTo(asset.getAddedAt());
    }
}
