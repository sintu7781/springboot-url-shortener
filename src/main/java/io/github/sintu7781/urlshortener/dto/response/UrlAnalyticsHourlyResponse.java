package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record UrlAnalyticsHourlyResponse(
        String shortCode,
        Instant from,
        Instant to,
        List<ClickHourlyTimeSeriesPoint> points
) {
}
