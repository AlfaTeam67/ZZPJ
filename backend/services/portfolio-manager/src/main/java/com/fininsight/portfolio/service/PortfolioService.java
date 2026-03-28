package com.fininsight.portfolio.service;

import com.fininsight.portfolio.dto.PortfolioRequest;
import com.fininsight.portfolio.dto.PortfolioResponse;
import com.fininsight.portfolio.entity.Portfolio;
import com.fininsight.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfoliosForUser(String userId) {
        return portfolioRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(Long id, String userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        return mapToResponse(portfolio);
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioRequest request, String userId) {
        Portfolio portfolio = Portfolio.builder()
                .name(request.getName())
                .userId(userId)
                .totalValue(request.getTotalValue())
                .build();
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(savedPortfolio);
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long id, PortfolioRequest request, String userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        
        portfolio.setName(request.getName());
        portfolio.setTotalValue(request.getTotalValue());
        
        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return mapToResponse(updatedPortfolio);
    }

    @Transactional
    public void deletePortfolio(Long id, String userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .userId(portfolio.getUserId())
                .totalValue(portfolio.getTotalValue())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}
