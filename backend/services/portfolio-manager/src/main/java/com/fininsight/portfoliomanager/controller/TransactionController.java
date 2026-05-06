package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.service.TransactionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Asset transaction APIs")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create transaction and update related asset position")
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
