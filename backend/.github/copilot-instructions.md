# Fin-Insight Java Development Rules

- Always use Java 21 LTS features (records, pattern matching).
- Use Spring Boot 3.x with Gradle (Groovy).
- STRICT RULE: Never use 'double' or 'float' for financial calculations. Always use 'java.math.BigDecimal'.
- Architecture: Microservices with Spring Cloud Eureka and Config Server.
- Security: OAuth2 Resource Server with Keycloak/JWT.
- Testing: Use JUnit 5, Mockito, and Cucumber for BDD.
- Database: Use Flyway for migrations.
