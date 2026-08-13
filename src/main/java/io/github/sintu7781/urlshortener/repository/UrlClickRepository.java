package io.github.sintu7781.urlshortener.repository;

import io.github.sintu7781.urlshortener.entity.UrlClick;
import io.github.sintu7781.urlshortener.repository.projection.*;
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

    @Query(value = """
            SELECT
                DATE_TRUNC('hour', c.clicked_at) AS hour,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.clicked_at >= :from
            AND c.clicked_at < :to
            GROUP BY DATE_TRUNC('hour', c.clicked_at)
            ORDER BY hour
            """, nativeQuery = true)
    List<ClickHourlyTimeSeriesProjection> findHourlyClickTimeSeries(
            @Param("urlId") Long urlId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query(value = """
            SELECT
                c.referrer AS referrer,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.referrer IS NOT NULL
            AND c.referrer <> ''
            GROUP BY c.referrer
            ORDER BY clicks DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ClickReferrerProjection> findTopReferrers(
            @Param("urlId") Long urlId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
                c.user_agent AS userAgent,
                count(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.user_agent IS NOT NULL
            AND c.user_agent <> ''
            GROUP BY c.user_agent
            ORDER BY clicks DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ClickUserAgentProjection> findTopUserAgents(
            @Param("urlId") Long urlId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
                c.browser AS browser,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.browser IS NOT NULL
            AND c.browser <> ''
            GROUP BY c.browser
            ORDER BY clicks DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ClickBrowserProjection> findTopBrowsers(
            @Param("urlId") Long urlId,
            @Param("limit") int limit
    );
}
