package com.fininsight.portfoliomanager.scheduler;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.service.PortfolioValuationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Records daily portfolio valuation snapshots at 23:59 each day.
 * Uses avg_buy_price * quantity as the cost-basis total (no live price call to avoid
 * transient MarketDataService dependency in the scheduler path).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioSnapshotScheduler {

    private final PortfolioValuationHistoryService historyService;
    private final PortfolioDataRepository portfolioRepository;
    private final AssetRepository assetRepository;

    @Scheduled(cron = "0 59 23 * * ?")
    public void recordDailySnapshots() {
        log.info("Starting daily portfolio valuation snapshots...");
        List<Portfolio> portfolios = portfolioRepository.findAll();

        for (Portfolio portfolio : portfolios) {
            try {
                BigDecimal totalValue = assetRepository
                    .findTotalValuesByPortfolioId(portfolio.getId())
                    .stream()
                    .map(AssetRepository.PortfolioCurrencyTotalValueProjection::getTotalValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                historyService.recordSnapshot(portfolio.getId(), totalValue);
            } catch (Exception e) {
                log.error("Failed to record snapshot for portfolio {}: {}", portfolio.getId(), e.getMessage(), e);
            }
        }
        log.info("Daily snapshots completed for {} portfolios", portfolios.size());
    }
}
