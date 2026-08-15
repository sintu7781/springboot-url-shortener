package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickRetentionCleanupService {

    private final UrlClickRepository urlClickRepository;

    @Value("${analytics.retention.click-days:90}")
    private long retentionDays;

    @Value("${analytics.retention.cleanup-batch-size:10000}")
    private int batchSize;

    @Transactional
    public int cleanupBatch() {

        Instant cutoff =
                Instant.now()
                        .minus(
                                retentionDays,
                                ChronoUnit.DAYS
                        );

        int deleted =
                urlClickRepository.deleteExpiredClicksBatch(
                        cutoff,
                        batchSize
                );

        log.info(
                "Click retention cleanup completed. cutoff={}, deleted={}, batchSize={}",
                cutoff,
                deleted,
                batchSize
        );

        return deleted;
    }

    public long countExpiredClicks() {

        Instant cutoff =
                Instant.now()
                        .minus(
                                retentionDays,
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
