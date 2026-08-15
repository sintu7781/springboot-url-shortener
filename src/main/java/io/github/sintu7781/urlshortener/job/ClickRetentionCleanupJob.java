package io.github.sintu7781.urlshortener.job;

import io.github.sintu7781.urlshortener.service.analytics.ClickRetentionCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickRetentionCleanupJob {

    private final ClickRetentionCleanupService cleanupService;

    @Scheduled(
            fixedDelayString =
                    "${analytics.retention.cleanup-interval-ms:3600000}"
    )
    @SchedulerLock(
            name = "clickRetentionCleanup",
            lockAtMostFor = "PT30M",
            lockAtLeastFor = "PT10S"
    )
    public void cleanExpiredClicks() {

        int deleted =
                cleanupService.cleanupBatch();

        log.info(
                "Click retention scheduled job completed. deleted={}",
                deleted
        );
    }
}
