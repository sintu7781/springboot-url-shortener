package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.config.AnalyticsRetentionProperties;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class ClickRetentionCleanupService {

    private final UrlClickRepository urlClickRepository;

    private final Counter deletedClicksCounter;

    private final AnalyticsRetentionProperties retentionProperties;

    public ClickRetentionCleanupService(
            UrlClickRepository urlClickRepository,
            MeterRegistry meterRegistry,
            AnalyticsRetentionProperties retentionProperties
    ) {

        this.urlClickRepository = urlClickRepository;

        this.retentionProperties = retentionProperties;

        this.deletedClicksCounter =
                Counter.builder(
                        "analytics.click.retention.deleted"
                )
                .description(
                        "Number of click events deleted by retention cleanup"
                )
                .register(meterRegistry);
    }

    @Transactional
    public int cleanupBatch() {

        Instant cutoff =
                Instant.now()
                        .minus(
                                retentionProperties.clickDays(),
                                ChronoUnit.DAYS
                        );

        int deleted =
                urlClickRepository.deleteExpiredClicksBatch(
                        cutoff,
                        retentionProperties.cleanupBatchSize()
                );

        if(deleted > 0) {

            deletedClicksCounter.increment(deleted);
        }

        log.info(
                "Click retention cleanup completed. cutoff={}, deleted={}, batchSize={}",
                cutoff,
                deleted,
                retentionProperties.cleanupBatchSize()
        );

        return deleted;
    }

    public long countExpiredClicks() {

        Instant cutoff =
                Instant.now()
                        .minus(
                                retentionProperties.clickDays(),
                                ChronoUnit.DAYS
                        );

        long count =
                urlClickRepository.countClicksBefore(
                        cutoff
                );

        log.info(
                "Click retention dry-run. cutoff={}, expiredClicks ={}",
                cutoff,
                count
        );

        return count;
    }
}
