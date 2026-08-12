package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

@Builder
public record ClickReferrerPoint(
        String referrer,
        long clicks
) {
}
