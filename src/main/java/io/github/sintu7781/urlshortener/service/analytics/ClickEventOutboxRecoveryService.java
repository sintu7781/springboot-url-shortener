package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import io.github.sintu7781.urlshortener.entity.ClickEventOutbox;
import io.github.sintu7781.urlshortener.repository.ClickEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventOutboxRecoveryService {

    private static final String PENDING = "PENDING";

    private static final String COMPLETED = "COMPLETED";

    private static final String FAILED = "FAILED";

    private final ClickEventOutboxRepository outboxRepository;

    private final ClickEventPublisher clickEventPublisher;

    @Transactional
    public int recoverBatch() {

        Instant now = Instant.now();

        List<ClickEventOutbox> events =
                outboxRepository
                        .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
                                PENDING,
                                now
                        );

        int recovered = 0;

        for(ClickEventOutbox outbox : events) {

            try {

                ClickEvent event =
                        new ClickEvent(
                                outbox.getEventId(),
                                outbox.getShortCode(),
                                outbox.getIpAddress(),
                                outbox.getUserAgent(),
                                outbox.getReferrer(),
                                outbox.getClickedAt()
                        );

                clickEventPublisher.publish(event);

                outbox.setStatus(COMPLETED);

                outbox.setProcessedAt(Instant.now());

                outbox.setLastError(null);

                recovered++;

            } catch (Exception ex) {

                outbox.setAttemptCount(
                        outbox.getAttemptCount() + 1
                );

                outbox.setLastError(
                        truncateError(ex)
                );

                outbox.setStatus(
                        outbox.getAttemptCount() >= 5
                                ? FAILED
                                : PENDING
                );

                outbox.setNextAttemptAt(
                        Instant.now().plusSeconds(30)
                );

                log.warn(
                        "Failed to recover click event from outbox. eventId={}, attemptCount={}",
                        outbox.getEventId(),
                        outbox.getAttemptCount(),
                        ex
                );
            }
        }

        return recovered;
    }

    private String truncateError(Exception ex) {

        String message = ex.getMessage();

        if(message == null || message.isBlank()) {

            return ex.getClass().getSimpleName();
        }

        return message.length() <= 2000
                ? message
                : message.substring(0, 2000);
    }

}
