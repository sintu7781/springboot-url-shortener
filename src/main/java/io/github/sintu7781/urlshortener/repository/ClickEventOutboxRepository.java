package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.ClickEventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ClickEventOutboxRepository
        extends JpaRepository<ClickEventOutbox, Long> {

    Optional<ClickEventOutbox> findByEventId(
            String eventId
    );

    List<ClickEventOutbox> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            String status,
            Instant nextAttemptAt
    );
}
