package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", uses = {AssetMapper.class})
public interface PortfolioMapper {
    @Mapping(target = "id", source = "portfolio.id")
    @Mapping(target = "userId", source = "portfolio.user.id")
    @Mapping(target = "name", source = "portfolio.name")
    @Mapping(target = "description", source = "portfolio.description")
    @Mapping(target = "assets", source = "portfolio.assets")
    @Mapping(target = "createdAt", source = "portfolio.createdAt")
    @Mapping(target = "totals", source = "totals")
    PortfolioResponse toResponse(Portfolio portfolio, Map<String, BigDecimal> totals);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totals", ignore = true)
    PortfolioResponse toResponse(Portfolio portfolio);

    List<PortfolioSummaryResponse> toSummaryList(List<Portfolio> portfolios);
}
