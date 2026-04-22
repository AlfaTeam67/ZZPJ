package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "portfolioId", source = "portfolio.id")
    @Mapping(target = "assetId", source = "asset.id")
    TransactionResponse toResponse(Transaction transaction);
}
