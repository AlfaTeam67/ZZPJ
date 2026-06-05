package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    @Operation(summary = "Get transaction history for a portfolio (paginated, filterable)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction history returned successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to this portfolio"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
        @PathVariable UUID portfolioId,
        @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field and direction, e.g. executedAt,desc") @RequestParam(defaultValue = "executedAt,desc") String sort,
        @Parameter(description = "Filter from date (ISO, e.g. 2024-01-01)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @Parameter(description = "Filter to date (ISO, e.g. 2024-12-31)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Parameter(description = "Filter by transaction type: BUY or SELL") @RequestParam(required = false) TransactionType type,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sort);
        Instant fromInstant = from != null ? from.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant toInstant   = to   != null ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        return ResponseEntity.ok(
            transactionService.getTransactionsByPortfolioPaged(portfolioId, userId, pageable, fromInstant, toInstant, type)
        );
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

    private Pageable buildPageable(int page, int size, String sort) {
        int clampedSize = Math.max(1, Math.min(size, 100));
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, clampedSize, Sort.by("executedAt").descending());
        }
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, clampedSize, Sort.by(direction, field));
    }
}
