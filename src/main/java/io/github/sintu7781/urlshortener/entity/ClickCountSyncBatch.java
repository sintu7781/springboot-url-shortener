package io.github.sintu7781.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_count_sync_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickCountSyncBatch {

    @Id
    @Column(
            name = "batch_key",
            nullable = false
    )
    private String batchKey;

    @Column(
            name = "short_code",
            nullable = false,
            length = 30
    )
    private String shortCode;

    @Column(
            name = "click_count",
            nullable = false
    )
    private long clickCount;

    @Column(
            name = "processed_at",
            nullable = false
    )
    private LocalDateTime processedAt;
}
