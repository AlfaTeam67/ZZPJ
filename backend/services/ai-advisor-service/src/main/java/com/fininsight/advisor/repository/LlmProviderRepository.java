package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.LlmProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LlmProviderRepository extends JpaRepository<LlmProvider, Short> {

    Optional<LlmProvider> findFirstByActiveTrueOrderByPriorityAsc();
}
