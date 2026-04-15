package com.fininsight.portfoliomanager.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fininsight.portfoliomanager.domain.enums.AssetType;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EntityValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void userShouldFailValidationWhenIdIsNull() {
        User user = new User();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void portfolioShouldFailValidationWhenNameExceedsLimit() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setName("x".repeat(101));

        Set<ConstraintViolation<Portfolio>> violations = validator.validate(portfolio);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void assetShouldFailValidationWhenCurrencyIsBlank() {
        Asset asset = new Asset();
        asset.setPortfolio(new Portfolio());
        asset.setType(AssetType.STOCK);
        asset.setSymbol("AAPL");
        asset.setQuantity(BigDecimal.ONE);
        asset.setAvgBuyPrice(BigDecimal.TEN);
        asset.setCurrency(" ");

        Set<ConstraintViolation<Asset>> violations = validator.validate(asset);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void transactionShouldPassValidationForValidEntity() {
        Transaction transaction = new Transaction();
        transaction.setAsset(new Asset());
        transaction.setPortfolio(new Portfolio());
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(new BigDecimal("1.25000000"));
        transaction.setPrice(new BigDecimal("123.4500"));
        transaction.setCurrency("USD");
        transaction.setFee(new BigDecimal("1.0000"));
        transaction.setExecutedAt(Instant.now());

        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);

        assertThat(violations).isEmpty();
    }

    @Test
    void financialFieldsShouldUseOnlyBigDecimal() {
        assertThat(hasFloatOrDoubleField(User.class)).isFalse();
        assertThat(hasFloatOrDoubleField(Portfolio.class)).isFalse();
        assertThat(hasFloatOrDoubleField(Asset.class)).isFalse();
        assertThat(hasFloatOrDoubleField(Transaction.class)).isFalse();
    }

    private static boolean hasFloatOrDoubleField(Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getType().equals(float.class)
                || field.getType().equals(double.class)
                || field.getType().equals(Float.class)
                || field.getType().equals(Double.class)) {
                return true;
            }
        }
        return false;
    }
}
