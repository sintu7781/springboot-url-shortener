package io.github.sintu7781.urlshortener.common.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
