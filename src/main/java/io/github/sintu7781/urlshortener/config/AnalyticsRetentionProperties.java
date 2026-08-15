package io.github.sintu7781.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics.retention")
public record AnalyticsRetentionProperties(
        long clickDays,
        int cleanupBatchSize,
        long cleanupIntervalMs
) {
}
