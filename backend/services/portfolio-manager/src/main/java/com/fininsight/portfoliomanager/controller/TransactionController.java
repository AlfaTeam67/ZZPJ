package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Asset transaction APIs")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get transaction history for a portfolio")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction history returned successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to this portfolio"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
        @PathVariable UUID portfolioId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transactionService.getTransactionsByPortfolio(portfolioId, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create transaction and update related asset position")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or business rule violation"),
        @ApiResponse(responseCode = "403", description = "Access denied to this portfolio"),
        @ApiResponse(responseCode = "404", description = "Portfolio or asset not found")
    })
    public ResponseEntity<TransactionResponse> createTransaction(
        @PathVariable UUID portfolioId,
        @Valid @RequestBody TransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        TransactionResponse response = transactionService.createTransaction(portfolioId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
