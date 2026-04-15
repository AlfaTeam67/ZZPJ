package pl.alfateam.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.alfateam.portfoliomanager.domain.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByPortfolioId(UUID portfolioId);

    List<Transaction> findByAssetId(UUID assetId);
}
