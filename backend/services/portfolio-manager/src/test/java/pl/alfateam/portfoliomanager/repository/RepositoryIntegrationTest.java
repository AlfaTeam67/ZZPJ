package pl.alfateam.portfoliomanager.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fininsight.portfolio.config.JpaAuditingConfig;
import pl.alfateam.portfoliomanager.domain.Asset;
import pl.alfateam.portfoliomanager.domain.Portfolio;
import pl.alfateam.portfoliomanager.domain.Transaction;
import pl.alfateam.portfoliomanager.domain.User;
import pl.alfateam.portfoliomanager.domain.enums.AssetType;
import pl.alfateam.portfoliomanager.domain.enums.TransactionType;
import pl.alfateam.portfoliomanager.domain.enums.UserRole;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = RepositoryIntegrationTest.TestApplication.class)
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/portfolio-model");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void shouldPersistAndReadEntitiesThroughRepositories() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("investor@example.com");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setName("Main Portfolio");
        portfolio.setDescription("Long term investing");
        portfolio = portfolioRepository.save(portfolio);

        Asset asset = new Asset();
        asset.setPortfolio(portfolio);
        asset.setType(AssetType.STOCK);
        asset.setSymbol("AAPL");
        asset.setQuantity(new BigDecimal("10.50000000"));
        asset.setAvgBuyPrice(new BigDecimal("155.1200"));
        asset.setCurrency("USD");
        asset = assetRepository.save(asset);

        Transaction transaction = new Transaction();
        transaction.setAsset(asset);
        transaction.setPortfolio(portfolio);
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(new BigDecimal("1.25000000"));
        transaction.setPrice(new BigDecimal("160.0000"));
        transaction.setCurrency("USD");
        transaction.setFee(new BigDecimal("0.8000"));
        transaction.setExecutedAt(Instant.now());
        transaction.setNotes("Initial buy");
        transactionRepository.save(transaction);

        List<Portfolio> portfolios = portfolioRepository.findByUserId(user.getId());
        List<Asset> assets = assetRepository.findByPortfolioId(portfolio.getId());
        List<Transaction> transactions = transactionRepository.findByPortfolioId(portfolio.getId());
        Optional<User> foundUser = userRepository.findByEmail("investor@example.com");

        assertThat(portfolios).hasSize(1);
        assertThat(assets).hasSize(1);
        assertThat(transactions).hasSize(1);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
        assertThat(portfolios.getFirst().getCreatedAt()).isNotNull();
        assertThat(assets.getFirst().getAddedAt()).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackageClasses = UserRepository.class)
    @EntityScan(basePackageClasses = User.class)
    @Import(JpaAuditingConfig.class)
    static class TestApplication {
    }
}
