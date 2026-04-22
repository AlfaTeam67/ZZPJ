package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.NewsCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NewsCacheRepository extends JpaRepository<NewsCache, UUID> {

    List<NewsCache> findByExpiresAtAfterOrderByFetchedAtDesc(Instant now);

    @Modifying
    @Query("DELETE FROM NewsCache n WHERE n.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
