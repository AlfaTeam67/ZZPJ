package com.fininsight.advisor.scheduler;

import com.fininsight.advisor.repository.NewsCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCleanupScheduler {

    private final NewsCacheRepository newsCacheRepository;

    @Scheduled(cron = "${app.scheduler.news-cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void deleteExpiredNews() {
        int deleted = newsCacheRepository.deleteExpired(Instant.now());
        log.info("NewsCleanupScheduler removed {} expired news entries", deleted);
    }
}
