package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.common.exception.UrlNotFoundException;
import io.github.sintu7781.urlshortener.dto.response.*;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final long MAX_HOURLY_RANGE_DAYS = 31;

    private static final long MAX_DAILY_RANGE_DAYS = 365;

    private static final long MAX_DASHBOARD_RANGE_DAYS = 31;

    private final UrlRepository urlRepository;

    private final UrlClickRepository urlClickRepository;

    private final AnalyticsCacheService analyticsCacheService;

    private UrlAnalyticsResponse getUrlAnalytics(
            Url url
    ) {

        long totalClicks =
                urlClickRepository.countByUrlId(
                        url.getId()
                );

        return UrlAnalyticsResponse.builder()
                .shortCode(url.getShortCode())
                .totalClicks(totalClicks)
                .lastClickedAt(
                        urlClickRepository.findLastClickedAt(
                                url.getId()
                        )
                )
                .build();
    }

    public UrlAnalyticsResponse getUrlAnalytics(
            String shortCode
    ) {

        Url url = findUrl(shortCode);

        return getUrlAnalytics(url);
    }

    private UrlAnalyticsRangeResponse getUrlAnalytics(
            Url url,
            Instant from,
            Instant to
    ) {

        long clicks =
                urlClickRepository
                        .countByUrlIdAndClickedAtGreaterThanEqualAndClickedAtLessThan(
                                url.getId(),
                                from,
                                to
                        );

        return UrlAnalyticsRangeResponse.builder()
                .shortCode(url.getShortCode())
                .from(from)
                .to(to)
                .clicks(clicks)
                .build();
    }

    public UrlAnalyticsRangeResponse getUrlAnalytics(
            String shortCode,
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        Url url = findUrl(shortCode);

        return getUrlAnalytics(
                url,
                from,
                to
        );
    }

    private UrlAnalyticsTimeSeriesResponse getUrlAnalyticsTimeSeries(
            Url url,
            AnalyticsContext context
    ) {

        List<ClickTimeSeriesPoint> points =
                urlClickRepository
                        .findDailyClickTimeSeries(
                                url.getId(),
                                context.from(),
                                context.to(),
                                context.timezone().getId()
                        )
                        .stream()
                        .map(point ->
                                ClickTimeSeriesPoint.builder()
                                        .date(point.getDate())
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsTimeSeriesResponse.builder()
                .shortCode(url.getShortCode())
                .from(context.from())
                .to(context.to())
                .points(points)
                .build();
    }

    public UrlAnalyticsTimeSeriesResponse getUrlAnalyticsTimeSeries(
            String shortCode,
            AnalyticsContext context
    ) {

        validateDailyTimeRange(
                context.from(),
                context.to()
        );

        Url url = findUrl(shortCode);

        return getUrlAnalyticsTimeSeries(
                url,
                context
        );
    }

    private UrlAnalyticsHourlyResponse getUrlAnalyticsHourly(
            Url url,
            AnalyticsContext context
    ) {

        List<ClickHourlyTimeSeriesPoint> points =
                urlClickRepository
                        .findHourlyClickTimeSeries(
                                url.getId(),
                                context.from(),
                                context.to(),
                                context.timezone().getId()
                        )
                        .stream()
                        .map(point ->
                                ClickHourlyTimeSeriesPoint.builder()
                                        .hour(point.getHour())
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsHourlyResponse.builder()
                .shortCode(url.getShortCode())
                .from(context.from())
                .to(context.to())
                .points(points)
                .build();
    }

    public UrlAnalyticsHourlyResponse getUrlAnalyticsHourly(
            String shortCode,
            AnalyticsContext context
    ) {

        validateHourlyTimeRange(
                context.from(),
                context.to()
        );

        Url url = findUrl(shortCode);

        return getUrlAnalyticsHourly(
                url,
                context
        );
    }

    private UrlAnalyticsReferrerResponse getUrlAnalyticsReferrers(
            Url url,
            int limit
    ) {

        List<ClickReferrerPoint> referrers =
                urlClickRepository
                        .findTopReferrers(
                                url.getId(),
                                limit
                        )
                        .stream()
                        .map(point ->
                                ClickReferrerPoint.builder()
                                        .referrer(point.getReferrer())
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsReferrerResponse.builder()
                .shortCode(url.getShortCode())
                .referrers(referrers)
                .build();
    }

    public UrlAnalyticsReferrerResponse getUrlAnalyticsReferrers(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

        return getUrlAnalyticsReferrers(
                url,
                limit
        );
    }

    public UrlAnalyticsUserAgentResponse getUrlAnalyticsUserAgents(
            Url url,
            int limit
    ) {

        List<ClickUserAgentPoint> userAgents =
                urlClickRepository
                        .findTopUserAgents(
                                url.getId(),
                                limit
                        )
                        .stream()
                        .map(point ->
                                ClickUserAgentPoint.builder()
                                        .userAgent(point.getUserAgent())
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsUserAgentResponse.builder()
                .shortCode(url.getShortCode())
                .userAgents(userAgents)
                .build();
    }

    public UrlAnalyticsUserAgentResponse getUrlAnalyticsUserAgents(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

        return getUrlAnalyticsUserAgents(
                url,
                limit
        );
    }

    private UrlAnalyticsBrowserResponse getUrlAnalyticsBrowsers(
            Url url,
            int limit
    ) {

        List<ClickBrowserPoint> browsers =
                urlClickRepository
                        .findTopBrowsers(
                                url.getId(),
                                limit
                        )
                        .stream()
                        .map(point ->
                                ClickBrowserPoint.builder()
                                        .browser(point.getBrowser())
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsBrowserResponse.builder()
                .shortCode(url.getShortCode())
                .browsers(browsers)
                .build();
    }

    public UrlAnalyticsBrowserResponse getUrlAnalyticsBrowsers(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

        return getUrlAnalyticsBrowsers(
                url,
                limit
        );
    }

    private UrlAnalyticsOperatingSystemResponse getUrlAnalyticsOperatingSystems(
            Url url,
            int limit
    ) {

        List<ClickOperatingSystemPoint> operatingSystems =
                urlClickRepository
                        .findTopOperatingSystems(
                                url.getId(),
                                limit
                        )
                        .stream()
                        .map(point ->
                                ClickOperatingSystemPoint.builder()
                                        .operatingSystem(
                                                point.getOperatingSystem()
                                        )
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsOperatingSystemResponse.builder()
                .shortCode(url.getShortCode())
                .operatingSystems(operatingSystems)
                .build();
    }

    public UrlAnalyticsOperatingSystemResponse getUrlAnalyticsOperatingSystems(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

        return getUrlAnalyticsOperatingSystems(
                url,
                limit
        );
    }

    private UrlAnalyticsDeviceResponse getUrlAnalyticsDevices(
            Url url,
            int limit
    ) {

        List<ClickDevicePoint> devices =
                urlClickRepository
                        .findTopDeviceTypes(
                                url.getId(),
                                limit
                        )
                        .stream()
                        .map(point ->
                                ClickDevicePoint.builder()
                                        .deviceType(
                                                point.getDeviceType()
                                        )
                                        .clicks(point.getClicks())
                                        .build()
                        )
                        .toList();

        return UrlAnalyticsDeviceResponse.builder()
                .shortCode(url.getShortCode())
                .devices(devices)
                .build();
    }

    public UrlAnalyticsDeviceResponse getUrlAnalyticsDevices(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

        return getUrlAnalyticsDevices(
                url,
                limit
        );
    }

    private UrlAnalyticsDashboardResponse buildDashboard(
            String shortCode,
            AnalyticsContext context,
            int limit
    ) {

        Url url = findUrl(shortCode);

        UrlAnalyticsResponse overview =
                getUrlAnalytics(url);

        UrlAnalyticsRangeResponse range =
                getUrlAnalytics(
                        url,
                        context.from(),
                        context.to()
                );

        UrlAnalyticsTimeSeriesResponse timeSeries =
                getUrlAnalyticsTimeSeries(
                        url,
                        context
                );

        UrlAnalyticsHourlyResponse hourly =
                getUrlAnalyticsHourly(
                        url,
                        context
                );

        UrlAnalyticsReferrerResponse referrers =
                getUrlAnalyticsReferrers(
                        url,
                        limit
                );

        UrlAnalyticsUserAgentResponse userAgents =
                getUrlAnalyticsUserAgents(
                        url,
                        limit
                );

        UrlAnalyticsBrowserResponse browsers =
                getUrlAnalyticsBrowsers(
                        url,
                        limit
                );

        UrlAnalyticsOperatingSystemResponse operatingSystems =
                getUrlAnalyticsOperatingSystems(
                        url,
                        limit
                );

        UrlAnalyticsDeviceResponse devices =
                getUrlAnalyticsDevices(
                        url,
                        limit
                );

        return UrlAnalyticsDashboardResponse.builder()
                .overview(overview)
                .range(range)
                .timeSeries(timeSeries)
                .hourly(hourly)
                .referrers(referrers)
                .userAgents(userAgents)
                .browsers(browsers)
                .operatingSystems(operatingSystems)
                .devices(devices)
                .build();
    }

    public UrlAnalyticsDashboardResponse getDashboard(
            String shortCode,
            AnalyticsContext context,
            int limit
    ) {

        validateDashboardTimeRange(
                context.from(),
                context.to()
        );

        validateLimit(limit);

        String timezone =
                context.timezone().getId();

        return analyticsCacheService
                .getDashboardWithLock(
                        shortCode,
                        context.from(),
                        context.to(),
                        timezone,
                        limit,
                        () -> buildDashboard(
                                shortCode,
                                context,
                                limit
                        )
                )
                .orElseThrow();

    }

    private void validateTimeRange(
            Instant from,
            Instant to
    ) {

        if(from == null || to == null) {

            throw new IllegalArgumentException(
                    "Both from and to are required."
            );
        }

        if(!from.isBefore(to)) {

            throw new IllegalArgumentException(
                    "'from' must be before 'to'."
            );
        }
    }

    private void validateDailyTimeRange(
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        if(Duration.between(from, to).toDays()
                > MAX_DAILY_RANGE_DAYS) {

            throw new IllegalArgumentException(
                    "Daily analytics range cannot exceed "
                            + MAX_DAILY_RANGE_DAYS
                            + " days."
            );
        }
    }

    private void validateHourlyTimeRange(
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        if(Duration.between(from, to).toDays()
                > MAX_HOURLY_RANGE_DAYS) {

            throw new IllegalArgumentException(
                    "Hourly analytics range cannot exceed "
                            + MAX_HOURLY_RANGE_DAYS
                            + " days."
            );
        }
    }

    private void validateDashboardTimeRange(
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        if(Duration.between(from, to).toDays()
                > MAX_DASHBOARD_RANGE_DAYS) {

            throw new IllegalArgumentException(
                    "Dashboard analytics range cannot exceed "
                            + MAX_DASHBOARD_RANGE_DAYS
                            + " days."
            );
        }
    }

    private Url findUrl(String shortCode) {

        validateShortCode(shortCode);

        return urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "URL not found: " + shortCode
                        )
                );
    }

    private void validateLimit(int limit) {

        if(limit < 1 || limit > 100) {

            throw new IllegalArgumentException(
                    "Limit must be between 1 and 100."
            );
        }

    }

    private void validateShortCode(String shortCode) {

        if(shortCode == null || shortCode.isEmpty()) {

            throw new IllegalArgumentException(
                    "Short code must not be blank."
            );
        }
    }
}
