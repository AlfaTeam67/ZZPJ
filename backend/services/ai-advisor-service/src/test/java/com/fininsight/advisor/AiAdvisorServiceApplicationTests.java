package com.fininsight.advisor;

import com.fininsight.advisor.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
class AiAdvisorServiceApplicationTests {

	@Test
	void contextLoads() {
		// Smoke test - verify that the application context loads successfully
	}

}
