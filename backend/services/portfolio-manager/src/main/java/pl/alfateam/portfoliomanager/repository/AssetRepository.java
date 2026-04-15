package pl.alfateam.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.alfateam.portfoliomanager.domain.Asset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByPortfolioId(UUID portfolioId);

    Optional<Asset> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
}
