package com.fininsight.marketdata.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
@Profile("!migration")
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api-docs/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        
        // Create a custom validator that accepts both keycloak:8080 and localhost:8080 issuers
        OAuth2TokenValidator<Jwt> multiIssuerValidator = new OAuth2TokenValidator<Jwt>() {
            private final Set<String> allowedIssuers = Set.of(
                issuerUri,
                issuerUri.replace("keycloak", "localhost")
            );
            
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                String tokenIssuer = token.getIssuer().toString();
                if (allowedIssuers.contains(tokenIssuer)) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", 
                        "The iss claim is not valid. Expected one of: " + allowedIssuers + " but got: " + tokenIssuer, 
                        null)
                );
            }
        };
        
        OAuth2TokenValidator<Jwt> withTimestamp = new DelegatingOAuth2TokenValidator<>(
            multiIssuerValidator,
            new JwtTimestampValidator()
        );
        
        jwtDecoder.setJwtValidator(withTimestamp);
        return jwtDecoder;
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Market Data Service API")
                .version("1.0.0")
                .description("REST API for Market Data Service in Fin-Insight platform")
                .contact(new Contact()
                    .name("Fin-Insight Team")
                    .email("support@fininsight.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addServersItem(new Server()
                .url("http://localhost:8082")
                .description("Local server"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT Authorization header using the Bearer scheme")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
