package com.fininsight.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fininsight.portfoliomanager.domain.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByPortfolioId(UUID portfolioId);

    List<Transaction> findByAssetId(UUID assetId);
}
