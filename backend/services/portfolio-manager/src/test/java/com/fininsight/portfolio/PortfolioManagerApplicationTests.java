package com.fininsight.portfolio;

import com.fininsight.portfolio.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class PortfolioManagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
