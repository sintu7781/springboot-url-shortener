package io.github.sintu7781.urlshortener.service.analytics;

public record UserAgentInfo(
        String browser,
        String operatingSystem,
        String deviceType
) {
}
