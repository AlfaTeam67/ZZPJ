package com.fininsight.portfoliomanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.portfoliomanager.dto.portfolio.CreatePortfolioRequest;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.UpdatePortfolioRequest;
import com.fininsight.portfoliomanager.dto.valuation.AssetValuationDto;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationResponse;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.service.PortfolioService;
import com.fininsight.portfoliomanager.service.ValuationService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://test-issuer"})
class PortfolioControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PORTFOLIO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private ValuationService valuationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void getAllPortfolios_returnsListWithStatus200() throws Exception {
        PortfolioResponse response = portfolioResponse(PORTFOLIO_ID, "Growth");
        when(portfolioService.getPortfoliosForUser(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/portfolios")
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(PORTFOLIO_ID.toString()))
            .andExpect(jsonPath("$[0].name").value("Growth"));
    }

    @Test
    void getPortfolioById_returnsPortfolioWithStatus200() throws Exception {
        PortfolioResponse response = portfolioResponse(PORTFOLIO_ID, "Growth");
        when(portfolioService.getPortfolio(PORTFOLIO_ID, USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/portfolios/{id}", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(PORTFOLIO_ID.toString()))
            .andExpect(jsonPath("$.name").value("Growth"));
    }

    @Test
    void getPortfolioById_returns404WhenNotFound() throws Exception {
        when(portfolioService.getPortfolio(PORTFOLIO_ID, USER_ID))
            .thenThrow(new PortfolioNotFoundException("Portfolio not found"));

        mockMvc.perform(get("/api/portfolios/{id}", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Portfolio not found"));
    }

    @Test
    void createPortfolio_returnsCreatedWithStatus201() throws Exception {
        CreatePortfolioRequest request = new CreatePortfolioRequest("Tech", "Long-term");
        PortfolioResponse response = portfolioResponse(PORTFOLIO_ID, "Tech");
        when(portfolioService.createPortfolio(any(CreatePortfolioRequest.class), eq(USER_ID))).thenReturn(response);

        mockMvc.perform(post("/api/portfolios")
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void createPortfolio_returns400WhenNameIsBlank() throws Exception {
        CreatePortfolioRequest request = new CreatePortfolioRequest("", null);

        mockMvc.perform(post("/api/portfolios")
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updatePortfolio_returnsUpdatedWithStatus200() throws Exception {
        UpdatePortfolioRequest request = new UpdatePortfolioRequest("Updated", "New desc");
        PortfolioResponse response = portfolioResponse(PORTFOLIO_ID, "Updated");
        when(portfolioService.updatePortfolio(eq(PORTFOLIO_ID), any(UpdatePortfolioRequest.class), eq(USER_ID)))
            .thenReturn(response);

        mockMvc.perform(put("/api/portfolios/{id}", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deletePortfolio_returnsNoContentWithStatus204() throws Exception {
        mockMvc.perform(delete("/api/portfolios/{id}", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isNoContent());

        verify(portfolioService).deletePortfolio(PORTFOLIO_ID, USER_ID);
    }

    @Test
    void deletePortfolio_returns404WhenNotFound() throws Exception {
        doThrow(new PortfolioNotFoundException("Portfolio not found"))
            .when(portfolioService).deletePortfolio(PORTFOLIO_ID, USER_ID);

        mockMvc.perform(delete("/api/portfolios/{id}", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPortfolioValuation_returnsValuationWithStatus200() throws Exception {
        var assetValuation = new AssetValuationDto(
            "AAPL", AssetType.STOCK,
            new BigDecimal("10"), new BigDecimal("150.00"),
            new BigDecimal("180.00"), new BigDecimal("1800.00"),
            new BigDecimal("300.00"), new BigDecimal("20.0000")
        );
        var response = new PortfolioValuationResponse(
            PORTFOLIO_ID, new BigDecimal("1800.00"), List.of(assetValuation), Instant.now()
        );
        when(valuationService.valuate(eq(PORTFOLIO_ID), eq(USER_ID), any())).thenReturn(response);

        mockMvc.perform(get("/api/portfolios/{id}/valuation", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID.toString()))
            .andExpect(jsonPath("$.totalValue").value(1800.00))
            .andExpect(jsonPath("$.assets[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$.assets[0].currentPrice").value(180.00));
    }

    @Test
    void getPortfolioValuation_returns401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/portfolios/{id}/valuation", PORTFOLIO_ID))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void anyEndpoint_returns401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/portfolios"))
            .andExpect(status().isUnauthorized());
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt()
            .jwt(j -> j.subject(USER_ID.toString()))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static PortfolioResponse portfolioResponse(UUID id, String name) {
        return new PortfolioResponse(id, USER_ID, name, null, List.of(), Map.of(), Instant.now());
    }
}
