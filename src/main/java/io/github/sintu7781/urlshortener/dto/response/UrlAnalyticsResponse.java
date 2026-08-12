package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UrlAnalyticsResponse(
        String shortCode,
        long totalClicks,
        Instant lastClickedAt
) {
}
