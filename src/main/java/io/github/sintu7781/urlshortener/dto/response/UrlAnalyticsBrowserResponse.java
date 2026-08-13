package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UrlAnalyticsBrowserResponse(
        String shortCode,
        List<ClickBrowserPoint> browsers
) {
}
