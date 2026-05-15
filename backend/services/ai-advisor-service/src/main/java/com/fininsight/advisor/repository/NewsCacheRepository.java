package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.enums.NewsProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NewsCacheRepository extends JpaRepository<NewsCache, UUID> {

    List<NewsCache> findByExpiresAtAfterOrderByFetchedAtDesc(Instant now);

    List<NewsCache> findBySymbolAndExpiresAtAfterOrderByFetchedAtDesc(String symbol, Instant now);

    Optional<NewsCache> findByProviderAndExternalId(NewsProvider provider, String externalId);

    @Modifying
    @Query("DELETE FROM NewsCache n WHERE n.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
