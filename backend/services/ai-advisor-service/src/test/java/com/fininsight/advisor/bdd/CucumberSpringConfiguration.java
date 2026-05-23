package com.fininsight.advisor.bdd;

import com.fininsight.advisor.client.PortfolioClient;
import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.client.news.FinnhubNewsClient;
import com.fininsight.advisor.client.news.NewsApiClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
@Import(CucumberSpringConfiguration.TestBeans.class)
public class CucumberSpringConfiguration {

    // Mockowane klienty zewnętrzne. Bean LlmChatClient zostaje podmieniony na mock,
    // co wyłącza prawdziwy OpenRouterClient i pozwala kontrolować odpowiedzi modelu w testach.
    @MockBean
    private LlmChatClient llmChatClient;

    @MockBean
    private PortfolioClient portfolioClient;

    @MockBean
    private FinnhubNewsClient finnhubNewsClient;

    @MockBean
    private NewsApiClient newsApiClient;

    @TestConfiguration
    static class TestBeans {
        /**
         * Decoder rozpoznaje token w postaci samego UUID i wstawia go jako sub.
         * Dzięki temu poszczególne scenariusze BDD mogą działać "z perspektywy" różnych userów
         * po prostu przekazując odpowiednie UUID w Authorization header.
         */
        @Bean
        @Primary
        public JwtDecoder jwtDecoder() {
            return token -> {
                String sub = token != null && !token.isBlank() ? token : "00000000-0000-0000-0000-000000000000";
                return Jwt.withTokenValue(token == null ? "" : token)
                    .header("alg", "none")
                    .claim("sub", sub)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            };
        }
    }
}
