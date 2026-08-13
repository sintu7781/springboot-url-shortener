package io.github.sintu7781.urlshortener.service.analytics;

public record AnalyticsCacheInvalidationEvent(
        String shortCode
) {
}
