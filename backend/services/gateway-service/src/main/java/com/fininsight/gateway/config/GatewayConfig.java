package com.fininsight.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Value("${gateway.rate-limit.replenish-rate:10}")
    private int replenishRate;

    @Value("${gateway.rate-limit.burst-capacity:20}")
    private int burstCapacity;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("portfolio-manager-api", r -> r
                .path("/api/portfolios/**", "/api-docs/portfolio/**", "/v3/api-docs/portfolio/**")
                .filters(f -> f
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .filter(this::rewriteSwaggerPath)
                )
                .uri("lb://portfolio-manager"))
            .route("market-data-api", r -> r
                .path("/api/market-prices/**", "/api/symbols/**", "/api/prices/**",
                      "/api-docs/market/**", "/v3/api-docs/market/**")
                .filters(f -> f
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .filter(this::rewriteSwaggerPath)
                )
                .uri("lb://market-data-service"))
            .route("advisor-api", r -> r
                .path("/api/recommendations/**",
                      "/api-docs/advisor/**", "/v3/api-docs/advisor/**")
                .filters(f -> f
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .filter(this::rewriteSwaggerPath)
                )
                .uri("lb://ai-advisor-service"))
            .build();
    }

    private Mono<Void> rewriteSwaggerPath(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getRawPath();

        String[] prefixes = {"portfolio", "market", "advisor"};
        for (String prefix : prefixes) {
            String swaggerPrefix = "/api-docs/" + prefix;
            String openApiPrefix = "/v3/api-docs/" + prefix;
            if (path.startsWith(swaggerPrefix + "/") || path.startsWith(openApiPrefix + "/")) {
                String newPath = path.replaceFirst("/" + prefix, "");
                ServerHttpRequest newRequest = request.mutate().path(newPath).build();
                return chain.filter(exchange.mutate().request(newRequest).build());
            }
        }
        return chain.filter(exchange);
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(replenishRate, burstCapacity);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
            .map(java.security.Principal::getName)
            .defaultIfEmpty("anonymous");
    }

    @Bean
    public WebFilter retryAfterFilter() {
        return (exchange, chain) -> chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                if (response.getStatusCode() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS) {
                    response.getHeaders().set(org.springframework.http.HttpHeaders.RETRY_AFTER, "1");
                }
            }));
    }

    private String extractJsonField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? null : json.substring(start, end);
    }
}
