package com.fininsight.portfoliomanager.repository;

import com.fininsight.portfoliomanager.domain.PortfolioValuationHistory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioValuationHistoryRepository extends JpaRepository<PortfolioValuationHistory, UUID> {

    List<PortfolioValuationHistory> findByPortfolioIdAndValuationDateBetween(
        UUID portfolioId,
        LocalDate from,
        LocalDate to,
        Sort sort
    );

    Optional<PortfolioValuationHistory> findByPortfolioIdAndValuationDate(
        UUID portfolioId,
        LocalDate date
    );
}
