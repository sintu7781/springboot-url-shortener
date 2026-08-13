package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record ClickBrowserPoint(
        String browser,
        long clicks
) {
}
