package io.github.sintu7781.urlshortener.common.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {
}
