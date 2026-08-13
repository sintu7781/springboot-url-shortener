package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record UrlAnalyticsDashboardResponse(
        UrlAnalyticsResponse overview,
        UrlAnalyticsRangeResponse range,
        UrlAnalyticsTimeSeriesResponse timeSeries,
        UrlAnalyticsHourlyResponse hourly,
        UrlAnalyticsReferrerResponse referrers,
        UrlAnalyticsBrowserResponse browsers,
        UrlAnalyticsOperatingSystemResponse operatingSystems,
        UrlAnalyticsDeviceResponse devices
) {
}
