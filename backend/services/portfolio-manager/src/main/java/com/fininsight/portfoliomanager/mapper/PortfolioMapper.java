package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {AssetMapper.class})
public interface PortfolioMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totals", ignore = true) // Set manually in service if needed
    PortfolioResponse toResponse(Portfolio portfolio);

    List<PortfolioSummaryResponse> toSummaryList(List<Portfolio> portfolios);
}
