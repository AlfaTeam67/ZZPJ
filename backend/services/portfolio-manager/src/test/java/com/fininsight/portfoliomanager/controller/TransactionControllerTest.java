package com.fininsight.portfoliomanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.transaction.TransactionRequest;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import com.fininsight.portfoliomanager.exception.PortfolioAccessDeniedException;
import com.fininsight.portfoliomanager.exception.PortfolioNotFoundException;
import com.fininsight.portfoliomanager.service.TransactionService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@TestPropertySource(properties = {"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://test-issuer"})
class TransactionControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PORTFOLIO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ASSET_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID TX_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void getTransactionHistory_returnsListWithStatus200() throws Exception {
        TransactionResponse response = transactionResponse(TX_ID, TransactionType.BUY);
        when(transactionService.getTransactionsByPortfolio(PORTFOLIO_ID, USER_ID))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(TX_ID.toString()))
            .andExpect(jsonPath("$[0].type").value("BUY"));
    }

    @Test
    void getTransactionHistory_returnsEmptyListWhenNoTransactions() throws Exception {
        when(transactionService.getTransactionsByPortfolio(PORTFOLIO_ID, USER_ID))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getTransactionHistory_returns404WhenPortfolioNotFound() throws Exception {
        when(transactionService.getTransactionsByPortfolio(PORTFOLIO_ID, USER_ID))
            .thenThrow(new PortfolioNotFoundException("Portfolio not found"));

        mockMvc.perform(get("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Portfolio not found"));
    }

    @Test
    void getTransactionHistory_returns403WhenAccessDenied() throws Exception {
        when(transactionService.getTransactionsByPortfolio(PORTFOLIO_ID, USER_ID))
            .thenThrow(new PortfolioAccessDeniedException("Access denied to this portfolio"));

        mockMvc.perform(get("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Access denied to this portfolio"));
    }

    @Test
    void createTransaction_returnsCreatedWithStatus201() throws Exception {
        TransactionRequest request = new TransactionRequest(
            ASSET_ID, TransactionType.BUY, new BigDecimal("5"), new BigDecimal("200.00"),
            "USD", null, null, null, null, null
        );
        TransactionResponse response = transactionResponse(TX_ID, TransactionType.BUY);
        when(transactionService.createTransaction(eq(PORTFOLIO_ID), eq(USER_ID), any(TransactionRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(TX_ID.toString()))
            .andExpect(jsonPath("$.type").value("BUY"));
    }

    @Test
    void createTransaction_returns400WhenTypeIsNull() throws Exception {
        String requestJson = """
            {"type": null, "quantity": 5, "price": 200.00, "currency": "USD"}
            """;

        mockMvc.perform(post("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID)
                .with(userJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void anyEndpoint_returns401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/portfolios/{portfolioId}/transactions", PORTFOLIO_ID))
            .andExpect(status().isUnauthorized());
    }

    private static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt() {
        return jwt()
            .jwt(j -> j.subject(USER_ID.toString()))
            .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static TransactionResponse transactionResponse(UUID id, TransactionType type) {
        return new TransactionResponse(
            id, PORTFOLIO_ID, ASSET_ID, "AAPL", AssetType.STOCK, type,
            new BigDecimal("5"), new BigDecimal("200.00"), "USD", null, Instant.now(), null
        );
    }
}
