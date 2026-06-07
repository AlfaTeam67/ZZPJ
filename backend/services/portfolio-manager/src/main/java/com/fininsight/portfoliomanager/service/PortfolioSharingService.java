package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.mapper.PortfolioMapper;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioSharingService {

    private static final String TOKEN_CHARS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 64;

    private final PortfolioDataRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final PortfolioMapper portfolioMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateShareToken(UUID portfolioId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        String token = buildSecureToken(TOKEN_LENGTH);
        portfolio.setShareToken(token);
        portfolio.setShareTokenCreatedAt(Instant.now());
        portfolioRepository.save(portfolio);
        return token;
    }

    @Transactional
    public void revokeShareToken(UUID portfolioId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied to this portfolio");
        }

        portfolio.setShareToken(null);
        portfolio.setShareTokenCreatedAt(null);
        portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getSharedPortfolio(String shareToken) {
        Portfolio portfolio = portfolioRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new PortfolioNotFoundException("Share token not found or revoked"));

        Map<String, BigDecimal> totals = assetRepository
            .findTotalValuesByPortfolioId(portfolio.getId())
            .stream()
            .collect(Collectors.toMap(
                AssetRepository.PortfolioCurrencyTotalValueProjection::getCurrency,
                AssetRepository.PortfolioCurrencyTotalValueProjection::getTotalValue
            ));
        return portfolioMapper.toResponse(portfolio, totals);
    }

    private String buildSecureToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TOKEN_CHARS.charAt(secureRandom.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }
}
