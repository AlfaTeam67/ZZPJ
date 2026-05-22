package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.dto.symbol.CreateSymbolRequest;
import com.fininsight.marketdata.dto.symbol.SymbolResponse;
import com.fininsight.marketdata.dto.symbol.UpdateSymbolRequest;
import com.fininsight.marketdata.exception.GlobalExceptionHandler.ErrorResponse;
import com.fininsight.marketdata.service.SymbolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
@Tag(name = "Symbols", description = "Supported trading symbol management")
@SecurityRequirement(name = "bearerAuth")
public class SymbolController {

    private final SymbolService symbolService;

    // -------------------------------------------------------------------------
    // GET /api/symbols
    // -------------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List all symbols", description = "Returns all supported trading symbols regardless of active status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Symbol list returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SymbolResponse.class))))
    })
    public ResponseEntity<List<SymbolResponse>> getAllSymbols() {
        return ResponseEntity.ok(symbolService.getAllSymbols());
    }

    // -------------------------------------------------------------------------
    // GET /api/symbols/{symbol}
    // -------------------------------------------------------------------------

    @GetMapping("/{symbol}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get symbol by ticker", description = "Returns a single symbol by its ticker code.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Symbol found",
            content = @Content(schema = @Schema(implementation = SymbolResponse.class))),
        @ApiResponse(responseCode = "404", description = "Symbol not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SymbolResponse> getSymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(symbolService.getSymbol(symbol));
    }

    // -------------------------------------------------------------------------
    // POST /api/symbols
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create symbol", description = "Registers a new supported symbol. Returns 409 if the ticker already exists.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(examples = @ExampleObject(name = "AAPL", value = """
            {
              "symbol": "AAPL",
              "type": "STOCK",
              "apiSource": "alphavantage",
              "active": true,
              "baseCurrency": "USD"
            }"""))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Symbol created",
            content = @Content(schema = @Schema(implementation = SymbolResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Symbol already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SymbolResponse> createSymbol(@Valid @RequestBody CreateSymbolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(symbolService.createSymbol(request));
    }

    // -------------------------------------------------------------------------
    // PUT /api/symbols/{symbol}
    // -------------------------------------------------------------------------

    @PutMapping("/{symbol}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update symbol", description = "Updates mutable fields (type, apiSource, active, baseCurrency). The ticker itself cannot be changed.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(examples = @ExampleObject(name = "Deactivate", value = """
            {
              "type": "STOCK",
              "apiSource": "alphavantage",
              "active": false,
              "baseCurrency": "USD"
            }"""))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Symbol updated",
            content = @Content(schema = @Schema(implementation = SymbolResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Symbol not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SymbolResponse> updateSymbol(
            @PathVariable String symbol,
            @Valid @RequestBody UpdateSymbolRequest request) {
        return ResponseEntity.ok(symbolService.updateSymbol(symbol, request));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/symbols/{symbol}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{symbol}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete symbol", description = "Removes a symbol and all its price snapshots (ON DELETE CASCADE). Cache entries are evicted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Symbol deleted"),
        @ApiResponse(responseCode = "404", description = "Symbol not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSymbol(@PathVariable String symbol) {
        symbolService.deleteSymbol(symbol);
        return ResponseEntity.noContent().build();
    }
}
