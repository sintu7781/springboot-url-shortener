package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record ClickDevicePoint(
        String deviceType,
        long clicks
) {
}
