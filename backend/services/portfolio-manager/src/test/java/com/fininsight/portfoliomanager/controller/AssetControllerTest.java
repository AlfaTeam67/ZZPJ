package com.fininsight.portfoliomanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import com.fininsight.portfoliomanager.exception.AssetNotFoundException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.service.AssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssetController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://test-issuer"})
class AssetControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PORTFOLIO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ASSET_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetService assetService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void addAsset_returnsCreatedWithStatus201() throws Exception {
        AddAssetRequest request = new AddAssetRequest(
            AssetType.STOCK, "AAPL", new BigDecimal("10"), new BigDecimal("150.00"), "USD"
        );
        AssetResponse response = assetResponse(ASSET_ID, "AAPL");
        when(assetService.addAsset(eq(PORTFOLIO_ID), any(AddAssetRequest.class), eq(USER_ID)))
            .thenReturn(response);

        mockMvc.perform(post("/api/portfolios/{portfolioId}/assets", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ASSET_ID.toString()))
            .andExpect(jsonPath("$.symbol").value("AAPL"));
    }

    @Test
    void addAsset_returns400WhenTypeIsNull() throws Exception {
        String requestJson = """
            {"type": null, "symbol": "AAPL", "quantity": 10, "avgBuyPrice": 150.00, "currency": "USD"}
            """;

        mockMvc.perform(post("/api/portfolios/{portfolioId}/assets", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void addAsset_returns400WhenQuantityIsTooSmall() throws Exception {
        AddAssetRequest request = new AddAssetRequest(
            AssetType.STOCK, "AAPL", new BigDecimal("0"), new BigDecimal("150.00"), "USD"
        );

        mockMvc.perform(post("/api/portfolios/{portfolioId}/assets", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    void addAsset_returns404WhenPortfolioNotFound() throws Exception {
        AddAssetRequest request = new AddAssetRequest(
            AssetType.STOCK, "AAPL", new BigDecimal("10"), new BigDecimal("150.00"), "USD"
        );
        when(assetService.addAsset(eq(PORTFOLIO_ID), any(AddAssetRequest.class), eq(USER_ID)))
            .thenThrow(new PortfolioNotFoundException("Portfolio not found"));

        mockMvc.perform(post("/api/portfolios/{portfolioId}/assets", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Portfolio not found"));
    }

    @Test
    void removeAsset_returnsNoContentWithStatus204() throws Exception {
        mockMvc.perform(delete("/api/portfolios/{portfolioId}/assets/{assetId}", PORTFOLIO_ID, ASSET_ID)
                .with(userJwt()))
            .andExpect(status().isNoContent());

        verify(assetService).removeAsset(PORTFOLIO_ID, ASSET_ID, USER_ID);
    }

    @Test
    void removeAsset_returns404WhenAssetNotFound() throws Exception {
        doThrow(new AssetNotFoundException("Asset not found"))
            .when(assetService).removeAsset(PORTFOLIO_ID, ASSET_ID, USER_ID);

        mockMvc.perform(delete("/api/portfolios/{portfolioId}/assets/{assetId}", PORTFOLIO_ID, ASSET_ID)
                .with(userJwt()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Asset not found"));
    }

    @Test
    void anyEndpoint_isBlockedWithoutAuthentication() throws Exception {
        // is4xxClientError() is intentionally used here instead of isUnauthorized() (401).
        // In @WebMvcTest, Spring Security's CSRF filter may return 403 FORBIDDEN for requests
        // without a CSRF token, even before the 401 authentication check. Using is4xxClientError()
        // accepts both 401 and 403, which correctly documents that the endpoint is protected.
        mockMvc.perform(delete("/api/portfolios/{portfolioId}/assets/{assetId}", PORTFOLIO_ID, ASSET_ID))
            .andExpect(status().is4xxClientError());
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt()
            .jwt(j -> j.subject(USER_ID.toString()))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static AssetResponse assetResponse(UUID id, String symbol) {
        return new AssetResponse(id, AssetType.STOCK, symbol, new BigDecimal("10"), new BigDecimal("150.00"), "USD", Instant.now());
    }
}
