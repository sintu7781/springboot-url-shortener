package io.github.sintu7781.urlshortener.job;

import io.github.sintu7781.urlshortener.service.analytics.ClickEventOutboxRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventOutboxRecoveryJob {

    private final ClickEventOutboxRecoveryService recoveryService;

    @Scheduled(
            fixedDelayString =
                    "${analytics.click-event-outbox.recovery-interval-ms:30000}"
    )
    @SchedulerLock(
            name = "clickEventOutboxRecovery",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT5S"
    )
    public void recover() {

        int recovered =
                recoveryService.recoverBatch();

        log.info(
                "Click event outbox recovery completed. recovered={}",
                recovered
        );
    }
}
