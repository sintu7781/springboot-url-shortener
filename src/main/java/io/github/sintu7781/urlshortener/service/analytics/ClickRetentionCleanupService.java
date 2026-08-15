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

    @Transactional
    public int cleanup() {

        Instant cutoff =
                Instant.now()
                        .minus(
                                retentionDays,
                                ChronoUnit.DAYS
                        );

        int deleted =
                urlClickRepository.deleteClicksBefore(
                        cutoff
                );

        log.info(
                "Click retention cleanup completed. cutoff={}, deleted={}",
                cutoff,
                deleted
        );

        return deleted;
    }
}
