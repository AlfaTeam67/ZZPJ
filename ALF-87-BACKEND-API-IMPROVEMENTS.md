# 📋 ALF-87 [Backend] Usprawnienia API — paginacja + historia portfela + feedback AI + bulk ops

## 🎯 CEL
Uzupełnić brakujące endpointy i usprawnić istniejące API w `portfolio-manager` i `ai-advisor-service`. Epic zawiera 5 podtasków, które mogą być **implementowane niezależnie i commitowane osobno**.

---

## 📊 ARCHITEKTURA PROJEKTU

### Struktura serwisów:
```
backend/services/
├── portfolio-manager/          (port 8081)
│   ├── src/main/java/.../portfoliomanager/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   ├── dto/
│   │   │   ├── portfolio/
│   │   │   ├── asset/
│   │   │   ├── transaction/
│   │   │   └── valuation/
│   │   └── exception/
│   ├── src/main/resources/db/migration/
│   │   └── V*.sql (Flyway)
│   └── build.gradle
│
└── ai-advisor-service/         (port 8083)
    ├── src/main/java/.../advisor/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/             (inne nazwy niż domain)
    │   ├── dto/
    │   └── scheduler/
    └── build.gradle
```

### Konwencje PROJEKTU:
- ✅ DTOs jako Java **records** (not classes): `public record PortfolioResponse(...)`
- ✅ Paginacja: `Page<T>` z Spring Data JPA (`Spring Pageable`)
- ✅ Migracje: Flyway, pliki `V{N}__description.sql`
- ✅ Mapowanie: MapStruct (`@Mapper`, `@Mapping`)
- ✅ Wyjątki: Custom `RuntimeException` (np. `PortfolioNotFoundException`)
- ✅ Auditing: `@CreatedDate`, `@LastModifiedDate` (Instant)
- ✅ Security: JWT Bearer Token, `@AuthenticationPrincipal Jwt jwt`
- ✅ OpenAPI: `@Operation`, `@ApiResponse`, `@Tag`

---

## 🎯 TASK-03.1: PAGINACJA ENDPOINTÓW LISTOWYCH

### Opis
Dodać obsługę paginacji do GET endpoints zwracających listy. Spring Data JPA `Pageable` wraz z `PagingAndSortingRepository`.

### Endpointy do modyfikacji

#### 1. GET /api/portfolios — Lista portfeli użytkownika
**Przed:**
```java
public ResponseEntity<List<PortfolioResponse>> getAllPortfolios(@AuthenticationPrincipal Jwt jwt)
```

**Po:**
```java
public ResponseEntity<Page<PortfolioResponse>> getAllPortfolios(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "createdAt,desc") String sort,
    @AuthenticationPrincipal Jwt jwt
)
```

**Response (200):**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {"sorted": true, "unsorted": false}
  },
  "totalElements": 50,
  "totalPages": 3,
  "last": false,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "first": true
}
```

#### 2. GET /api/portfolios/{id}/assets — Lista aktywów w portfelu
**Nowe parametry:** `page`, `size`, `sort`

#### 3. GET /api/portfolios/{id}/transactions — Historia transakcji
**Nowe parametry:** `page`, `size`, `sort`, `from` (ISO date), `to` (ISO date)

**Dodać filtrowanie:**
```java
@RequestParam(required = false) String from,  // 2024-01-01
@RequestParam(required = false) String to,    // 2024-12-31
@RequestParam(required = false) String type   // BUY, SELL
```

### Implementacja

#### 1. Zaktualizuj Repository
```java
// Było:
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    List<Portfolio> findByUserId(UUID userId);
}

// Będzie:
public interface PortfolioRepository extends PagingAndSortingRepository<Portfolio, UUID>, JpaRepository<Portfolio, UUID> {
    Page<Portfolio> findByUserId(UUID userId, Pageable pageable);
}
```

Similarly dla `AssetRepository`, `TransactionRepository`.

#### 2. TransactionRepository — Dodaj specjalne metody
```java
Page<Transaction> findByPortfolioId(UUID portfolioId, Pageable pageable);

@Query("SELECT t FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
       "AND (:from IS NULL OR t.executedAt >= :from) " +
       "AND (:to IS NULL OR t.executedAt <= :to) " +
       "AND (:type IS NULL OR t.type = :type)")
Page<Transaction> findByPortfolioIdWithFilters(
    @Param("portfolioId") UUID portfolioId,
    @Param("from") Instant from,
    @Param("to") Instant to,
    @Param("type") TransactionType type,
    Pageable pageable
);
```

#### 3. Service Layer — Deleguj Pageable
```java
@Service
public class PortfolioService {
    @Transactional(readOnly = true)
    public Page<PortfolioResponse> getAllPortfolios(UUID userId, Pageable pageable) {
        return portfolioRepository.findByUserId(userId, pageable)
            .map(portfolioMapper::toResponse);
    }
}
```

#### 4. Controller — Akceptuj Pageable
```java
@GetMapping
public ResponseEntity<Page<PortfolioResponse>> getAllPortfolios(
    Pageable pageable,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(portfolioService.getAllPortfolios(userId, pageable));
}
```

### Tests
- [ ] Test `GET /api/portfolios?page=0&size=10` → status 200, pagination metadata
- [ ] Test sorting: `sort=name,asc`
- [ ] Test invalid page → status 400
- [ ] Test transactions filtering: `from=2024-01-01&to=2024-12-31&type=BUY`

### Commit
```bash
git commit -m "feat(portfolio-api): add pagination to list endpoints (portfolios, assets, transactions)"
```

---

## 🎯 TASK-03.2: PORTFOLIO PERFORMANCE HISTORY

### Opis
Historia snapshot'ów wartości portfela (daily snapshots) z endpointem GET i schedulorem.

### Implementacja

#### 1. Nowa encja — PortfolioValuationHistory
```java
package com.fininsight.portfoliomanager.domain;

@Entity
@Table(name = "portfolio_valuation_history")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioValuationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @NotNull
    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate;  // Snapshot datetime

    @NotNull
    @Column(name = "total_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalValue;  // USDT, EUR, itp — waluta z portfolio.currency

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Unique constraint: jeden snapshot per portfolio per day
    @UniqueConstraint(columnNames = {"portfolio_id", "valuation_date"})
}
```

#### 2. Migracja Flyway — V5__portfolio_valuation_history.sql
```sql
CREATE TABLE portfolio_valuation_history (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    valuation_date DATE NOT NULL,
    total_value NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(portfolio_id, valuation_date)
);

CREATE INDEX idx_portfolio_valuation_portfolio_id ON portfolio_valuation_history(portfolio_id);
CREATE INDEX idx_portfolio_valuation_date ON portfolio_valuation_history(valuation_date);
```

#### 3. Repository
```java
public interface PortfolioValuationHistoryRepository extends JpaRepository<PortfolioValuationHistory, UUID> {
    List<PortfolioValuationHistory> findByPortfolioIdAndValuationDateBetween(
        UUID portfolioId,
        LocalDate from,
        LocalDate to,
        Sort sort  // czy Pageable?
    );
    
    Optional<PortfolioValuationHistory> findByPortfolioIdAndValuationDate(
        UUID portfolioId,
        LocalDate date
    );
}
```

#### 4. DTO
```java
public record PortfolioValuationHistoryResponse(
    UUID id,
    UUID portfolioId,
    LocalDate valuationDate,
    BigDecimal totalValue,
    Instant createdAt
) {}
```

#### 5. Service
```java
@Service
@RequiredArgsConstructor
public class PortfolioValuationHistoryService {

    private final PortfolioValuationHistoryRepository historyRepository;
    private final PortfolioDataRepository portfolioRepository;
    private final ValuationService valuationService;

    @Transactional
    public void recordDailySnapshot(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        // Wylicz bieżącą wartość
        PortfolioValuationResponse valuation = valuationService.calculateValuation(portfolioId);
        
        // Sprawdź czy snapshot na dziś już istnieje
        LocalDate today = LocalDate.now();
        historyRepository.findByPortfolioIdAndValuationDate(portfolioId, today)
            .ifPresentOrElse(
                existing -> {
                    // Update jeśli istnieje
                    existing.setTotalValue(valuation.totalValue());
                    historyRepository.save(existing);
                },
                () -> {
                    // Utwórz nowy
                    PortfolioValuationHistory newHistory = new PortfolioValuationHistory();
                    newHistory.setPortfolio(portfolio);
                    newHistory.setValuationDate(today);
                    newHistory.setTotalValue(valuation.totalValue());
                    historyRepository.save(newHistory);
                }
            );
    }

    @Transactional(readOnly = true)
    public List<PortfolioValuationHistoryResponse> getHistory(
        UUID portfolioId,
        UUID userId,
        LocalDate from,
        LocalDate to
    ) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied");
        }
        
        return historyRepository
            .findByPortfolioIdAndValuationDateBetween(portfolioId, from, to, Sort.by("valuationDate").ascending())
            .stream()
            .map(this::toResponse)
            .toList();
    }
}
```

#### 6. Scheduler — Codzienne snapshoty
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotScheduler {

    private final PortfolioValuationHistoryService historyService;
    private final PortfolioDataRepository portfolioRepository;

    // Codziennie o 23:59
    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void recordDailySnapshots() {
        log.info("Starting daily portfolio valuation snapshots...");
        List<Portfolio> portfolios = portfolioRepository.findAll();
        portfolios.forEach(p -> {
            try {
                historyService.recordDailySnapshot(p.getId());
            } catch (Exception e) {
                log.error("Failed to record snapshot for portfolio {}", p.getId(), e);
            }
        });
        log.info("Daily snapshots completed");
    }
}
```

#### 7. Controller
```java
@GetMapping("/{id}/history")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "Get portfolio valuation history")
public ResponseEntity<List<PortfolioValuationHistoryResponse>> getPortfolioHistory(
    @PathVariable UUID id,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(1);
    LocalDate toDate = to != null ? to : LocalDate.now();
    
    return ResponseEntity.ok(
        portfolioService.getPortfolioHistory(id, userId, fromDate, toDate)
    );
}
```

### Tests
- [ ] POST snapshot dla portfolio → czy jest w DB
- [ ] GET /api/portfolios/{id}/history → lista z datami
- [ ] Filtrowanie po date range
- [ ] Scheduler — test mock time

### Commit
```bash
git commit -m "feat(portfolio): add portfolio valuation history with daily snapshots and scheduler"
```

---

## 🎯 TASK-03.3: AI ADVISOR — FEEDBACK ENDPOINT

### Opis
Endpoint do oceny rekomendacji (like/dislike + optional komentarz), przechowywanie feedback w DB.

### Implementacja

#### 1. Nowa encja — RecommendationFeedback
```java
package com.fininsight.advisor.entity;

@Entity
@Table(name = "recommendation_feedbacks")
@Getter
@Setter
@NoArgsConstructor
public class RecommendationFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @NotNull
    @Column(name = "is_positive", nullable = false)
    private Boolean isPositive;  // true = thumbs up, false = thumbs down

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;  // Optional user comment

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

#### 2. Migracja Flyway — V7__recommendation_feedbacks.sql
```sql
CREATE TABLE recommendation_feedbacks (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    is_positive BOOLEAN NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_rec_feedback_rec FOREIGN KEY (recommendation_id) REFERENCES recommendations(id)
);

CREATE INDEX idx_rec_feedback_recommendation_id ON recommendation_feedbacks(recommendation_id);
CREATE INDEX idx_rec_feedback_created_at ON recommendation_feedbacks(created_at DESC);
```

#### 3. Repository
```java
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {
    List<RecommendationFeedback> findByRecommendationId(UUID recommendationId);
    
    long countByRecommendationIdAndIsPositive(UUID recommendationId, Boolean isPositive);
}
```

#### 4. DTOs
```java
// Request
public record RecommendationFeedbackRequest(
    @NotNull
    Boolean isPositive,
    
    @Size(max = 500)
    String comment
) {}

// Response
public record RecommendationFeedbackResponse(
    UUID id,
    UUID recommendationId,
    Boolean isPositive,
    String comment,
    Instant createdAt
) {}

// Statystyka (opcjonalne)
public record RecommendationFeedbackStats(
    UUID recommendationId,
    Long totalFeedback,
    Long positiveFeedback,
    Long negativeFeedback,
    Double positiveRatio
) {}
```

#### 5. Service
```java
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationFeedbackService {

    private final RecommendationFeedbackRepository feedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationFeedbackMapper mapper;

    public RecommendationFeedbackResponse submitFeedback(
        UUID recommendationId,
        UUID userId,
        RecommendationFeedbackRequest request
    ) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new NotFoundException("Recommendation not found"));
        
        // Verify ownership
        if (!recommendation.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setRecommendation(recommendation);
        feedback.setIsPositive(request.isPositive());
        feedback.setComment(request.comment());

        RecommendationFeedback saved = feedbackRepository.save(feedback);
        
        // TODO: Update LLM prompt context (include feedback in next recommendations)
        return mapper.toResponse(saved);
    }

    public List<RecommendationFeedbackResponse> getFeedbackForRecommendation(
        UUID recommendationId
    ) {
        return feedbackRepository.findByRecommendationId(recommendationId)
            .stream()
            .map(mapper::toResponse)
            .toList();
    }

    public RecommendationFeedbackStats getStatistics(UUID recommendationId) {
        long total = feedbackRepository.count();
        long positive = feedbackRepository.countByRecommendationIdAndIsPositive(recommendationId, true);
        long negative = total - positive;
        
        return new RecommendationFeedbackStats(
            recommendationId,
            total,
            positive,
            negative,
            total > 0 ? (double) positive / total : 0.0
        );
    }
}
```

#### 6. Controller
```java
@RestController
@RequestMapping("/api/recommendations/{id}/feedback")
@RequiredArgsConstructor
public class RecommendationFeedbackController {

    private final RecommendationFeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Submit feedback for a recommendation")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Feedback submitted"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    public ResponseEntity<RecommendationFeedbackResponse> submitFeedback(
        @PathVariable UUID id,
        @Valid @RequestBody RecommendationFeedbackRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        RecommendationFeedbackResponse response = feedbackService.submitFeedback(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get feedback for a recommendation")
    public ResponseEntity<List<RecommendationFeedbackResponse>> getFeedback(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbackForRecommendation(id));
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get feedback statistics")
    public ResponseEntity<RecommendationFeedbackStats> getStats(
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(feedbackService.getStatistics(id));
    }
}
```

### Tests
- [ ] POST feedback → status 201
- [ ] GET feedback → lista
- [ ] GET stats → ratio obliczony poprawnie
- [ ] Unauthorized user → 403

### Commit
```bash
git commit -m "feat(advisor): add recommendation feedback endpoint with stats"
```

---

## 🎯 TASK-03.4: BULK OPERATIONS

### Opis
Dodanie wielu aktywów lub transakcji naraz (batch).

### Implementacja

#### 1. DTOs
```java
// Bulk add assets
public record BulkAssetRequest(
    @NotEmpty
    List<AssetRequest> assets
) {}

public record BulkAssetResponse(
    List<AssetResponse> successful,
    List<BulkError> errors
) {}

// Bulk create transactions
public record BulkTransactionRequest(
    @NotEmpty
    List<TransactionRequest> transactions
) {}

public record BulkTransactionResponse(
    List<TransactionResponse> successful,
    List<BulkError> errors,
    int successCount,
    int errorCount
) {}

public record BulkError(
    int index,
    String field,
    String message
) {}
```

#### 2. Service
```java
@Service
@RequiredArgsConstructor
public class BulkOperationService {

    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioDataRepository portfolioRepository;

    @Transactional
    public BulkAssetResponse bulkAddAssets(UUID portfolioId, UUID userId, BulkAssetRequest request) {
        Portfolio portfolio = verifyPortfolioOwnership(portfolioId, userId);
        
        List<AssetResponse> successful = new ArrayList<>();
        List<BulkError> errors = new ArrayList<>();

        for (int i = 0; i < request.assets().size(); i++) {
            try {
                AssetRequest assetReq = request.assets().get(i);
                // Validate
                validateAssetRequest(assetReq);
                
                Asset asset = new Asset();
                asset.setPortfolio(portfolio);
                // ... mapuj z assetReq
                
                Asset saved = assetRepository.save(asset);
                successful.add(assetMapper.toResponse(saved));
            } catch (ConstraintViolationException | IllegalArgumentException e) {
                errors.add(new BulkError(i, "asset", e.getMessage()));
            }
        }

        return new BulkAssetResponse(successful, errors);
    }

    @Transactional
    public BulkTransactionResponse bulkAddTransactions(
        UUID portfolioId,
        UUID userId,
        BulkTransactionRequest request
    ) {
        Portfolio portfolio = verifyPortfolioOwnership(portfolioId, userId);
        
        List<TransactionResponse> successful = new ArrayList<>();
        List<BulkError> errors = new ArrayList<>();

        for (int i = 0; i < request.transactions().size(); i++) {
            try {
                TransactionRequest txReq = request.transactions().get(i);
                // ... walidacja + tworzenie
                Transaction tx = createTransaction(portfolio, txReq);
                successful.add(transactionMapper.toResponse(tx));
            } catch (Exception e) {
                errors.add(new BulkError(i, "transaction", e.getMessage()));
            }
        }

        return new BulkTransactionResponse(
            successful,
            errors,
            successful.size(),
            errors.size()
        );
    }

    private Portfolio verifyPortfolioOwnership(UUID portfolioId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied");
        }
        
        return portfolio;
    }

    private void validateAssetRequest(AssetRequest request) {
        if (request.symbol() == null || request.symbol().isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        // ... więcej walidacji
    }

    private Transaction createTransaction(Portfolio portfolio, TransactionRequest request) {
        // ... logika tworzenia transakcji
        return null;
    }
}
```

#### 3. Controller
```java
@PostMapping("/{portfolioId}/assets/bulk")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "Bulk add assets to portfolio")
public ResponseEntity<BulkAssetResponse> bulkAddAssets(
    @PathVariable UUID portfolioId,
    @Valid @RequestBody BulkAssetRequest request,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    BulkAssetResponse response = bulkOperationService.bulkAddAssets(portfolioId, userId, request);
    return ResponseEntity.ok(response);
}

@PostMapping("/{portfolioId}/transactions/bulk")
@PreAuthorize("hasRole('USER')")
@Operation(summary = "Bulk add transactions to portfolio (e.g., CSV import)")
public ResponseEntity<BulkTransactionResponse> bulkAddTransactions(
    @PathVariable UUID portfolioId,
    @Valid @RequestBody BulkTransactionRequest request,
    @AuthenticationPrincipal Jwt jwt
) {
    UUID userId = UUID.fromString(jwt.getSubject());
    BulkTransactionResponse response = bulkOperationService.bulkAddTransactions(portfolioId, userId, request);
    return ResponseEntity.ok(response);
}
```

### Tests
- [ ] POST assets/bulk with 5 assets → 4 successful, 1 error
- [ ] POST transactions/bulk → partial success counted
- [ ] Error list zawiera index i message

### Commit
```bash
git commit -m "feat(portfolio): add bulk operations for assets and transactions with partial success handling"
```

---

## 🎯 TASK-03.5: PORTFOLIO SHARING / READ-ONLY ACCESS

### Opis
Generuj shareable link do read-only widoku portfela (bez auth).

### Implementacja

#### 1. Zaktualizuj Portfolio encję
```java
@Entity
@Table(name = "portfolios")
public class Portfolio {
    // ... istniejące pola
    
    @Column(name = "share_token", unique = true, length = 128)
    private String shareToken;  // NULL = nie udostępniony

    @Column(name = "share_token_created_at")
    private Instant shareTokenCreatedAt;
}
```

#### 2. Migracja — Dodaj kolumny
```sql
ALTER TABLE portfolios ADD COLUMN share_token VARCHAR(128) UNIQUE;
ALTER TABLE portfolios ADD COLUMN share_token_created_at TIMESTAMP;

CREATE INDEX idx_portfolios_share_token ON portfolios(share_token);
```

#### 3. Service
```java
@Service
@RequiredArgsConstructor
public class PortfolioSharingService {

    private final PortfolioDataRepository portfolioRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateShareToken(UUID portfolioId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied");
        }

        String token = generateSecureToken(128);
        portfolio.setShareToken(token);
        portfolio.setShareTokenCreatedAt(Instant.now());
        portfolioRepository.save(portfolio);

        return token;
    }

    @Transactional
    public void revokeShareToken(UUID portfolioId, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));
        
        if (!portfolio.getUser().getId().equals(userId)) {
            throw new PortfolioAccessDeniedException("Access denied");
        }

        portfolio.setShareToken(null);
        portfolio.setShareTokenCreatedAt(null);
        portfolioRepository.save(portfolio);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getSharedPortfolio(String shareToken) {
        Portfolio portfolio = portfolioRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new PortfolioNotFoundException("Share token not found"));
        
        return portfolioMapper.toResponse(portfolio);
    }

    private String generateSecureToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < length; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        return token.toString();
    }
}
```

#### 4. Repository
```java
public interface PortfolioRepository extends PagingAndSortingRepository<Portfolio, UUID>, JpaRepository<Portfolio, UUID> {
    // ... istniejące
    Optional<Portfolio> findByShareToken(String shareToken);
}
```

#### 5. Controller — Publicznie dostępne
```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicPortfolioController {

    private final PortfolioSharingService sharingService;

    @GetMapping("/public/portfolios/{shareToken}")
    @Operation(summary = "Get shared portfolio (no auth required)")
    @ApiResponse(responseCode = "200", description = "Shared portfolio data")
    public ResponseEntity<PortfolioResponse> getSharedPortfolio(
        @PathVariable String shareToken
    ) {
        return ResponseEntity.ok(sharingService.getSharedPortfolio(shareToken));
    }
}

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    // ... istniejące

    @PostMapping("/{id}/share")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Generate share token for portfolio")
    public ResponseEntity<Map<String, String>> generateShareToken(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String token = portfolioSharingService.generateShareToken(id, userId);
        return ResponseEntity.ok(Map.of(
            "shareToken", token,
            "shareUrl", "https://yourapp.com/shared/" + token
        ));
    }

    @DeleteMapping("/{id}/share")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Revoke share token")
    public ResponseEntity<Void> revokeShareToken(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        portfolioSharingService.revokeShareToken(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

### Tests
- [ ] POST /api/portfolios/{id}/share → token generated
- [ ] GET /api/public/portfolios/{token} → portfolio visible without auth
- [ ] DELETE /api/portfolios/{id}/share → GET token returns 404
- [ ] Invalid token → 404

### Commit
```bash
git commit -m "feat(portfolio): add shareable links for read-only portfolio access"
```

---

## ✅ EXECUTION PLAN

Każdy task może być commitowany **niezależnie i równolegle**:

### Order (sugerowany):
1. **TASK-03.1** (Paginacja) — Foundation, najprostsza
2. **TASK-03.2** (History) — Zależy od paginacji
3. **TASK-03.3** (Feedback) — Niezależny (ai-advisor service)
4. **TASK-03.4** (Bulk) — Zależy od istniejących services
5. **TASK-03.5** (Sharing) — Zależy od Portfolio

### Per commit:
```bash
# TASK-03.1
git add backend/services/portfolio-manager/src/
git commit -m "feat(portfolio-api): add pagination to list endpoints (portfolios, assets, transactions)"

# TASK-03.2
git add backend/services/portfolio-manager/src/
git commit -m "feat(portfolio): add portfolio valuation history with daily snapshots and scheduler"

# TASK-03.3
git add backend/services/ai-advisor-service/src/
git commit -m "feat(advisor): add recommendation feedback endpoint with stats"

# TASK-03.4
git add backend/services/portfolio-manager/src/
git commit -m "feat(portfolio): add bulk operations for assets and transactions with partial success handling"

# TASK-03.5
git add backend/services/portfolio-manager/src/
git commit -m "feat(portfolio): add shareable links for read-only portfolio access"
```

---

## 🔗 DODATKOWE WYTYCZNE

1. **Java Version**: 21 (LTS)
2. **Spring Boot**: 3.2+
3. **Kotlin-like records**: `public record ` zamiast classes
4. **Lombok**: `@Getter`, `@Setter`, `@RequiredArgsConstructor`
5. **Validation**: `@NotNull`, `@NotEmpty`, `@Valid`
6. **Security**: JWT, `@PreAuthorize("hasRole('USER')")`
7. **OpenAPI**: `@Operation`, `@ApiResponse`, `@Tag`
8. **Auditing**: `@CreatedDate`, `@LastModifiedDate`, `@EntityListeners(AuditingEntityListener.class)`
9. **Error Handling**: Custom exceptions, `GlobalExceptionHandler`
10. **Testing**: `@WebMvcTest`, MockMvc, TestPropertySource

---

**Gotowy do implementacji!** 🚀
