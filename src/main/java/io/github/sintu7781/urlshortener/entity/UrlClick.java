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

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "url_id",
            nullable = false
    )
    private Url url;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "referrer", length = 2000)
    private String referrer;
}
