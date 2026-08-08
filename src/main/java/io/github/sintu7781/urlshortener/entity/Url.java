package io.github.sintu7781.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "urls",
        indexes = {
                @Index(name = "idx_short_code", columnList = "shortCode", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {

//    @Id
//    private Long id;
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "url_seq_generator"
    )
    @SequenceGenerator(
            name = "url_seq_generator",
            sequenceName = "url_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
