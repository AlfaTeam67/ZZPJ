package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.PortfolioValuationHistory;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationHistoryResponse;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.PortfolioValuationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioValuationHistoryService {

    private final PortfolioValuationHistoryRepository historyRepository;
    private final PortfolioDataRepository portfolioRepository;

    /**
     * Records a daily valuation snapshot for a portfolio.
     * If a snapshot for today already exists, it is updated.
     * Called by the scheduler with totalValue pre-calculated from ValuationService.
     */
    @Transactional
    public void recordSnapshot(UUID portfolioId, BigDecimal totalValue) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found: " + portfolioId));

        LocalDate today = LocalDate.now();
        historyRepository.findByPortfolioIdAndValuationDate(portfolioId, today)
            .ifPresentOrElse(
                existing -> {
                    existing.setTotalValue(totalValue);
                    historyRepository.save(existing);
                    log.debug("Updated valuation snapshot for portfolio {} on {}", portfolioId, today);
                },
                () -> {
                    PortfolioValuationHistory history = new PortfolioValuationHistory();
                    history.setPortfolio(portfolio);
                    history.setValuationDate(today);
                    history.setTotalValue(totalValue);
                    historyRepository.save(history);
                    log.debug("Created valuation snapshot for portfolio {} on {}", portfolioId, today);
                }
            );
    }

    @Transactional(readOnly = true)
    public List<PortfolioValuationHistoryResponse> getHistory(
        UUID portfolioId,
        UUID userId,
        LocalDate from,
        LocalDate to
    ) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        return historyRepository
            .findByPortfolioIdAndValuationDateBetween(portfolioId, from, to, Sort.by("valuationDate").ascending())
            .stream()
            .map(h -> new PortfolioValuationHistoryResponse(
                h.getId(),
                h.getPortfolio().getId(),
                h.getValuationDate(),
                h.getTotalValue(),
                h.getCreatedAt()
            ))
            .toList();
    }
}
