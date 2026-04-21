package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.dto.PortfolioRequest;
import com.fininsight.portfoliomanager.dto.PortfolioResponse;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioDataRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfoliosForUser(String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userUuid);
        if (portfolios.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<AssetRepository.PortfolioCurrencyTotalValueProjection>> totalsByPortfolioId = assetRepository.findTotalValuesByPortfolioIds(
            portfolios.stream().map(Portfolio::getId).toList()
        ).stream().collect(Collectors.groupingBy(
            AssetRepository.PortfolioCurrencyTotalValueProjection::getPortfolioId,
            Collectors.toList()
        ));

        return portfolios.stream()
            .map(portfolio -> mapToResponse(portfolio, resolveTotals(totalsByPortfolioId.get(portfolio.getId()))))
            .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(UUID id, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        return mapToResponse(portfolio, resolveTotals(assetRepository.findTotalValuesByPortfolioId(portfolio.getId())));
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioRequest request, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        User user = findOrCreateUser(userUuid);

        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setUser(user);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(savedPortfolio, Map.of());
    }

    @Transactional
    public PortfolioResponse updatePortfolio(UUID id, PortfolioRequest request, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(updatedPortfolio, resolveTotals(assetRepository.findTotalValuesByPortfolioId(updatedPortfolio.getId())));
    }

    @Transactional
    public void deletePortfolio(UUID id, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio, Map<String, BigDecimal> totals) {
        return PortfolioResponse.builder()
            .id(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .userId(portfolio.getUser().getId())
            .totals(totals)
            .createdAt(portfolio.getCreatedAt())
            .build();
    }

    private Map<String, BigDecimal> resolveTotals(List<AssetRepository.PortfolioCurrencyTotalValueProjection> totalsByCurrency) {
        if (totalsByCurrency == null || totalsByCurrency.isEmpty()) {
            return Map.of();
        }
        return totalsByCurrency.stream()
            .collect(Collectors.toMap(
                AssetRepository.PortfolioCurrencyTotalValueProjection::getCurrency,
                AssetRepository.PortfolioCurrencyTotalValueProjection::getTotalValue
            ));
    }

    private User findOrCreateUser(UUID userUuid) {
        return userRepository.findById(userUuid)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setId(userUuid);
                try {
                    return userRepository.saveAndFlush(newUser);
                } catch (DataIntegrityViolationException exception) {
                    return userRepository.findById(userUuid)
                        .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "User provisioning conflict"
                        ));
                }
            });
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
