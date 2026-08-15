package io.github.sintu7781.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "click_event_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_click_event_outbox_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_click_event_outbox_pending",
                        columnList = "status, next_attempt_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(
            name = "event_id",
            nullable = false,
            length = 36
    )
    private String eventId;

    @Column(
            name = "short_code",
            nullable = false,
            length = 30
    )
    private String shortCode;

    @Column(
            name = "clicked_at",
            nullable = false
    )
    private Instant clickedAt;

    @Column(
            name = "ip_address",
            length = 45
    )
    private String ipAddress;

    @Column(
            name = "user_agent",
            length = 1000
    )
    private String userAgent;

    @Column(
            name = "referrer",
            length = 2000
    )
    private String referrer;

    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private String status = "PENDING";
    @Column(
            name = "attempt_count",
            nullable = false
    )
    @Builder.Default
    private int attemptCount = 0;

    @Column(
            name = "next_attempt_at",
            nullable = false
    )
    private Instant nextAttemptAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(
            name = "last_error",
            length = 2000
    )
    private String lastError;

    @PrePersist
    void onCreate() {

        Instant now = Instant.now();

        if(createdAt == null) {

            createdAt = now;
        }

        if(nextAttemptAt == null) {

            nextAttemptAt = now;
        }
    }
}
