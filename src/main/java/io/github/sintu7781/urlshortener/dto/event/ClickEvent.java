package io.github.sintu7781.urlshortener.dto.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ClickEvent(
        String shortCode,
        String ipAddress,
        String userAgent,
        String referrer,
        Instant clickedAt
) {
}
