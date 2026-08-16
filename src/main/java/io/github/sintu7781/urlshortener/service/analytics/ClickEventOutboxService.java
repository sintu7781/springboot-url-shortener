package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import io.github.sintu7781.urlshortener.entity.ClickEventOutbox;
import io.github.sintu7781.urlshortener.repository.ClickEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventOutboxService {

    private final ClickEventOutboxRepository outboxRepository;

    public void saveFallback(
            ClickEvent event,
            Exception cause
    ) {

        if(outboxRepository.findByEventId(event.eventId()).isPresent()) {

            log.debug(
                    "Click event already exists in outbox. eventId={}",
                    event.eventId()
            );

            return;
        }

        ClickEventOutbox outbox =
                ClickEventOutbox.builder()
                        .eventId(event.eventId())
                        .shortCode(event.shortCode())
                        .clickedAt(event.clickedAt())
                        .ipAddress(event.ipAddress())
                        .userAgent(event.userAgent())
                        .referrer(event.referrer())
                        .status("PENDING")
                        .attemptCount(0)
                        .nextAttemptAt(Instant.now())
                        .lastError(truncateError(cause))
                        .build();

        outboxRepository.save(outbox);

        log.warn(
                "Click event stored in PostgreSQL outbox after Redis publish failure. eventId={}, shortCode={}",
                event.eventId(),
                event.shortCode()
        );
    }

    private String truncateError(Exception cause) {

        String message = cause.getMessage();

        if(message == null || message.isBlank()) {

            return cause.getClass().getSimpleName();
        }

        return message.length() <= 2000
                ? message
                : message.substring(0, 2000);
    }
}
