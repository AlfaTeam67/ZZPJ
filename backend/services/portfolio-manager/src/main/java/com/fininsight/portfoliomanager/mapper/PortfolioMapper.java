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
    PortfolioResponse toResponse(Portfolio portfolio, java.util.Map<String, java.math.BigDecimal> totals);

    List<PortfolioSummaryResponse> toSummaryList(List<Portfolio> portfolios);
}
