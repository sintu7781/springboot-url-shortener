package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.*;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlRepository urlRepository;

    private final UrlClickRepository urlClickRepository;

    public UrlAnalyticsResponse getUrlAnalytics(
            String shortCode
    ) {

        Url url = findUrl(shortCode);

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

    public UrlAnalyticsRangeResponse getUrlAnalytics(
            String shortCode,
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        Url url = findUrl(shortCode);

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

    public UrlAnalyticsTimeSeriesResponse getUrlAnalyticsTimeSeries(
            String shortCode,
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        Url url = findUrl(shortCode);

        List<ClickTimeSeriesPoint> points =
                urlClickRepository
                        .findDailyClickTimeSeries(
                                url.getId(),
                                from,
                                to
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
                .from(from)
                .to(to)
                .points(points)
                .build();
    }

    public UrlAnalyticsHourlyResponse getUrlAnalyticsHourly(
            String shortCode,
            Instant from,
            Instant to
    ) {

        validateTimeRange(from, to);

        Url url = findUrl(shortCode);

        List<ClickHourlyTimeSeriesPoint> points =
                urlClickRepository
                        .findHourlyClickTimeSeries(
                                url.getId(),
                                from,
                                to
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
                .from(from)
                .to(to)
                .points(points)
                .build();
    }

    public UrlAnalyticsReferrerResponse getUrlAnalyticsReferrers(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

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

    public UrlAnalyticsUserAgentResponse getUrlAnalyticsUserAgents(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

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

    public UrlAnalyticsBrowserResponse getUrlAnalyticsBrowsers(
            String shortCode,
            int limit
    ) {

        validateLimit(limit);

        Url url = findUrl(shortCode);

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
                .shortCode(shortCode)
                .browsers(browsers)
                .build();
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

    private Url findUrl(String shortCode) {

        return urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
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
}
