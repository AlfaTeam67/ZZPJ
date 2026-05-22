package com.fininsight.advisor.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Dwa rodzaje RestClient.Builder:
 *  - load-balanced (Eureka) - do wywołań innych serwisów po nazwie (np. portfolio-manager)
 *  - plain - do wywołań zewnętrznych (OpenRouter, Finnhub, NewsAPI)
 */
@Configuration
class RestClientConfig {

    @Bean
    @LoadBalanced
    @Scope("prototype")
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder().requestFactory(buildFactory(Duration.ofSeconds(3), Duration.ofSeconds(5)));
    }

    @Bean
    @Scope("prototype")
    RestClient.Builder externalRestClientBuilder() {
        return RestClient.builder().requestFactory(buildFactory(Duration.ofSeconds(5), Duration.ofSeconds(30)));
    }

    private SimpleClientHttpRequestFactory buildFactory(Duration connect, Duration read) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connect);
        factory.setReadTimeout(read);
        return factory;
    }
}
