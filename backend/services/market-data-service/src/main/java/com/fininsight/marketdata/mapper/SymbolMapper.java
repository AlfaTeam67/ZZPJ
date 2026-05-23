package com.fininsight.marketdata.mapper;

import com.fininsight.marketdata.dto.symbol.CreateSymbolRequest;
import com.fininsight.marketdata.dto.symbol.SymbolResponse;
import com.fininsight.marketdata.entity.SupportedSymbol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SymbolMapper {

    SymbolResponse toResponse(SupportedSymbol entity);

    List<SymbolResponse> toResponseList(List<SupportedSymbol> entities);

    @Mapping(target = "addedAt", ignore = true)
    SupportedSymbol toEntity(CreateSymbolRequest request);

    /**
     * Applies fields from {@code request} onto an existing managed entity.
     * The primary key (symbol) stays unchanged — only mutable fields are updated.
     */
    @Mapping(target = "symbol", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    void updateEntity(com.fininsight.marketdata.dto.symbol.UpdateSymbolRequest request,
                      @MappingTarget SupportedSymbol entity);
}
