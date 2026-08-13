package io.github.sintu7781.urlshortener.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsCacheInvalidationListener {

    private final AnalyticsCacheService analyticsCacheService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            AnalyticsCacheInvalidationEvent event
    ) {

        log.debug(
                "Invalidating analytics cache after transaction commit. shortCode={}",
                event.shortCode()
        );

        analyticsCacheService.evictDashboard(
                event.shortCode()
        );
    }
}
