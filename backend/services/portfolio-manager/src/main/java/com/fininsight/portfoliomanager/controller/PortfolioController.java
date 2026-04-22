package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.portfolio.CreatePortfolioRequest;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.UpdatePortfolioRequest;
import com.fininsight.portfoliomanager.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all portfolios for the authenticated user")
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(portfolioService.getAllPortfoliosForUser(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<PortfolioResponse> getPortfolioById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(portfolioService.getPortfolioById(id, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(
        @Valid @RequestBody CreatePortfolioRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createPortfolio(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update an existing portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePortfolioRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> deletePortfolio(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        portfolioService.deletePortfolio(id, userId);
        return ResponseEntity.noContent().build();
    }
}
