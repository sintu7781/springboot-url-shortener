package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.UrlClick;
import io.github.sintu7781.urlshortener.repository.projection.ClickTimeSeriesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface UrlClickRepository
        extends JpaRepository<UrlClick, Long> {

    boolean existsByEventId(String eventId);

    long countByUrlId(Long urlId);

    long countByUrlIdAndClickedAtGreaterThanEqualAndClickedAtLessThan(
            Long urlId,
            Instant from,
            Instant to
    );

    @Query("""
            SELECT MAX(c.clickedAt)
            FROM UrlClick c
            WHERE c.url.id = :urlId
    """)
    Instant findLastClickedAt(
            @Param("urlId") Long urlId
    );

    @Query(value = """
            SELECT
                CAST(c.clicked_at AS DATE) AS date,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.clicked_at >= :from
            AND c.clicked_at < :to
            GROUP BY CAST(c.clicked_at AS DATE)
            ORDER BY date
            """, nativeQuery = true)
    List<ClickTimeSeriesProjection> findDailyClickTimeSeries(
            @Param("urlId") Long urlId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
