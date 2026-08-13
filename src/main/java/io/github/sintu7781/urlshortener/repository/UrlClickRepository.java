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
                click_date AS date,
                COUNT(*) AS clicks
            FROM (
                SELECT
                    CAST(
                        c.clicked_at AT TIME ZONE :timezone
                        AS DATE
                    ) AS click_date
                FROM url_clicks c
                WHERE c.url_id = :urlId
                AND c.clicked_at >= :from
                AND c.clicked_at < :to
            ) daily_clicks
            GROUP BY click_date
            ORDER BY click_date
            """, nativeQuery = true)
    List<ClickTimeSeriesProjection> findDailyClickTimeSeries(
            @Param("urlId") Long urlId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("timezone") String timezone
    );

    @Query(value = """
            SELECT
                click_hour AS hour,
                COUNT(*) AS clicks
            FROM (
                SELECT
                    DATE_TRUNC(
                        'hour',
                        c.clicked_at AT TIME ZONE :timezone
                    ) AS click_hour
                FROM url_clicks c
                WHERE c.url_id = :urlId
                AND c.clicked_at >= :from
                AND c.clicked_at < :to
            ) hourly_clicks
            GROUP BY click_hour
            ORDER BY click_hour
            """, nativeQuery = true)
    List<ClickHourlyTimeSeriesProjection> findHourlyClickTimeSeries(
            @Param("urlId") Long urlId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("timezone") String timezone
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

    @Query(value = """
            SELECT
                c.operating_system AS operatingSystem,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.operating_system IS NOT NULL
            AND c.operating_system <> ''
            GROUP BY c.operating_system
            ORDER BY clicks DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ClickOperatingSystemProjection> findTopOperatingSystems(
            @Param("urlId") Long urlId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT
                c.device_type AS deviceType,
                COUNT(*) AS clicks
            FROM url_clicks c
            WHERE c.url_id = :urlId
            AND c.device_type IS NOT NULL
            AND c.device_type <> ''
            GROUP BY c.device_type
            ORDER BY clicks DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ClickDeviceProjection> findTopDeviceTypes(
            @Param("urlId") Long urlId,
            @Param("limit") int limit
    );
}
