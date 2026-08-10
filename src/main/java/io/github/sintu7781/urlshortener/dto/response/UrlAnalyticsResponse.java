package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record UrlAnalyticsResponse(
        String shortCode,
        String originalUrl,
        Long clickCount
) {
}
