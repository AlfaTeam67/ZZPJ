package com.fininsight.portfoliomanager.mapper;

import com.fininsight.portfoliomanager.domain.Asset;
import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import com.fininsight.portfoliomanager.dto.transaction.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void shouldMapTransactionToTransactionResponse() {
        // given
        UUID transactionId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        Asset asset = new Asset();
        asset.setId(assetId);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(new BigDecimal("5.0"));
        transaction.setPrice(new BigDecimal("100.0"));
        transaction.setCurrency("USD");
        transaction.setFee(new BigDecimal("1.5"));
        transaction.setExecutedAt(Instant.now());
        transaction.setNotes("Test transaction");

        // when
        TransactionResponse response = mapper.toResponse(transaction);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(transaction.getId());
        assertThat(response.portfolioId()).isEqualTo(portfolioId);
        assertThat(response.assetId()).isEqualTo(assetId);
        assertThat(response.type()).isEqualTo(transaction.getType());
        assertThat(response.quantity()).isEqualByComparingTo(transaction.getQuantity());
        assertThat(response.price()).isEqualByComparingTo(transaction.getPrice());
        assertThat(response.currency()).isEqualTo(transaction.getCurrency());
        assertThat(response.fee()).isEqualByComparingTo(transaction.getFee());
        assertThat(response.executedAt()).isEqualTo(transaction.getExecutedAt());
        assertThat(response.notes()).isEqualTo(transaction.getNotes());
    }
}
