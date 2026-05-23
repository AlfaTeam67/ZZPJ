package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.dto.price.CreateMarketPriceRequest;
import com.fininsight.marketdata.dto.price.LatestPriceResponse;
import com.fininsight.marketdata.dto.price.MarketPriceResponse;
import com.fininsight.marketdata.exception.GlobalExceptionHandler.ErrorResponse;
import com.fininsight.marketdata.service.MarketPriceService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/market-prices")
@RequiredArgsConstructor
@Tag(name = "Market Prices", description = "Market price snapshot management")
@SecurityRequirement(name = "bearerAuth")
public class MarketPriceController {

    private final MarketPriceService priceService;

    // -------------------------------------------------------------------------
    // GET /api/market-prices/latest
    // -------------------------------------------------------------------------

    @GetMapping("/latest")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Latest prices for all active symbols",
               description = "Returns the most recent price snapshot for every active symbol.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Prices returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarketPriceResponse.class))))
    })
    public ResponseEntity<List<MarketPriceResponse>> getLatestPrices() {
        return ResponseEntity.ok(priceService.getLatestPrices());
    }

    // -------------------------------------------------------------------------
    // GET /api/market-prices/symbol/{ticker}/latest
    // Stable contract consumed by portfolio-manager's MarketDataClient
    // -------------------------------------------------------------------------

    @GetMapping("/symbol/{ticker}/latest")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Latest price for a symbol",
        description = """
            Returns the single latest price for the requested symbol.
            Cache hit is indicated by the `cached` flag.
            **This endpoint is the stable contract consumed by portfolio-manager.**
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Latest price returned",
            content = @Content(schema = @Schema(implementation = LatestPriceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Symbol not found or no price data available",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LatestPriceResponse> getLatestPriceBySymbol(@PathVariable String ticker) {
        return ResponseEntity.ok(priceService.getLatestPriceBySymbol(ticker));
    }

    // -------------------------------------------------------------------------
    // GET /api/market-prices/symbol/{ticker}
    // History endpoint kept for backward-compatibility
    // -------------------------------------------------------------------------

    @GetMapping("/symbol/{ticker}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Price history for a symbol",
               description = "Returns all recorded price snapshots for an active symbol, newest first.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Price history returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MarketPriceResponse.class)))),
        @ApiResponse(responseCode = "404", description = "Symbol not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<MarketPriceResponse>> getPriceHistory(@PathVariable String ticker) {
        return ResponseEntity.ok(priceService.getPriceHistoryBySymbol(ticker));
    }

    // -------------------------------------------------------------------------
    // POST /api/market-prices
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Record a new price snapshot",
               description = "Saves a new price snapshot and updates the Redis cache for the affected symbol.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(examples = @ExampleObject(name = "AAPL snapshot", value = """
            {
              "symbol": "AAPL",
              "source": "alphavantage",
              "price": 182.50,
              "currency": "USD",
              "changePct24h": 1.25,
              "volume24h": 75000000.00
            }"""))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Snapshot recorded",
            content = @Content(schema = @Schema(implementation = MarketPriceResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or symbol not active",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Symbol not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MarketPriceResponse> addMarketPrice(
            @Valid @RequestBody CreateMarketPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priceService.addPrice(request));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/market-prices/{id}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete a price snapshot",
               description = "Removes a specific snapshot by UUID and evicts the cache for the related symbol.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Snapshot deleted"),
        @ApiResponse(responseCode = "404", description = "Snapshot not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteSnapshot(@PathVariable UUID id) {
        priceService.deleteSnapshot(id);
        return ResponseEntity.noContent().build();
    }
}
