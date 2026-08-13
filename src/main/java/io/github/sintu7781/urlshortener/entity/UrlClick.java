package io.github.sintu7781.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "url_clicks",
        indexes = {
                @Index(
                        name = "idx_url_click_url_id",
                        columnList = "url_id"
                ),

                @Index(
                        name = "idx_url_click_clicked_at",
                        columnList = "clicked_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_url_clicks_event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlClick {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "url_click_seq_generator"
    )
    @SequenceGenerator(
            name = "url_click_seq_generator",
            sequenceName = "url_click_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String eventId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "url_id",
            nullable = false
    )
    private Url url;

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
            name = "browser",
            length = 100
    )
    private String browser;

    @Column(
            name = "operating_system",
            length = 100
    )
    private String operatingSystem;

    @Column(
            name = "device_type",
            length = 50
    )
    private String deviceType;

    @Column(
            name = "referrer",
            length = 2000
    )
    private String referrer;
}
