package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.dto.PortfolioRequest;
import com.fininsight.portfoliomanager.dto.PortfolioResponse;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        Map<UUID, BigDecimal> totalsByPortfolioId = assetRepository.findTotalValuesByPortfolioIds(
            portfolios.stream().map(Portfolio::getId).toList()
        ).stream().collect(Collectors.toMap(
            AssetRepository.PortfolioTotalValueProjection::getPortfolioId,
            AssetRepository.PortfolioTotalValueProjection::getTotalValue
        ));

        return portfolios.stream()
            .map(portfolio -> mapToResponse(portfolio, totalsByPortfolioId.getOrDefault(portfolio.getId(), BigDecimal.ZERO)))
            .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(UUID id, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        return mapToResponse(portfolio, assetRepository.calculateTotalValueByPortfolioId(portfolio.getId()));
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioRequest request, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        User user = userRepository.findById(userUuid).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(userUuid);
            return userRepository.save(newUser);
        });

        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());
        portfolio.setUser(user);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(savedPortfolio, assetRepository.calculateTotalValueByPortfolioId(savedPortfolio.getId()));
    }

    @Transactional
    public PortfolioResponse updatePortfolio(UUID id, PortfolioRequest request, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));

        portfolio.setName(request.getName());
        portfolio.setDescription(request.getDescription());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(updatedPortfolio, assetRepository.calculateTotalValueByPortfolioId(updatedPortfolio.getId()));
    }

    @Transactional
    public void deletePortfolio(UUID id, String userId) {
        UUID userUuid = parseUuid(userId, "Invalid user ID");
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userUuid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio, BigDecimal totalValue) {
        return PortfolioResponse.builder()
            .id(portfolio.getId())
            .name(portfolio.getName())
            .description(portfolio.getDescription())
            .userId(portfolio.getUser().getId())
            .totalValue(totalValue)
            .createdAt(portfolio.getCreatedAt())
            .build();
    }

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
