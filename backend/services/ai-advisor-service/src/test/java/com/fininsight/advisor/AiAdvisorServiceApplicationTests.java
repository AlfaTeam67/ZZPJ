package com.fininsight.advisor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9999/realms/test"
})
class AiAdvisorServiceApplicationTests {

	@Test
	void contextLoads() {
		// Smoke test - verify that the application context loads successfully
	}

}
