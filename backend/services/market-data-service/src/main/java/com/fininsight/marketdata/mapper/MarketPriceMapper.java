package com.fininsight.marketdata.mapper;

import com.fininsight.marketdata.dto.price.CreateMarketPriceRequest;
import com.fininsight.marketdata.dto.price.LatestPriceResponse;
import com.fininsight.marketdata.dto.price.MarketPriceResponse;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketPriceMapper {

    // PriceSnapshot.symbol is a SupportedSymbol entity — we want the string ticker in the response
    @Mapping(target = "symbol", source = "snapshot.symbol.symbol")
    MarketPriceResponse toResponse(PriceSnapshot snapshot);

    List<MarketPriceResponse> toResponseList(List<PriceSnapshot> snapshots);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "symbol", source = "symbolEntity")
    @Mapping(target = "fetchedAt", source = "request.fetchedAt")
    PriceSnapshot toEntity(CreateMarketPriceRequest request, SupportedSymbol symbolEntity);

    @Mapping(target = "symbol", source = "snapshot.symbol.symbol")
    @Mapping(target = "cached", constant = "false")
    LatestPriceResponse toLatestResponse(PriceSnapshot snapshot);
}
