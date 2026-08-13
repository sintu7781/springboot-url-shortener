package io.github.sintu7781.urlshortener.service.analytics;

import java.time.Instant;
import java.time.ZoneId;

public record AnalyticsContext(
        Instant from,
        Instant to,
        ZoneId timezone
) {
}
