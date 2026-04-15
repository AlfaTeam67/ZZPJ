package pl.alfateam.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.alfateam.portfoliomanager.domain.Portfolio;

import java.util.List;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    List<Portfolio> findByUserId(UUID userId);
}
