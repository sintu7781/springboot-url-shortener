package io.github.sintu7781.urlshortener.service.analytics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsMetrics {

    private final Counter cacheHits;

    private final Counter cacheMisses;

    private final Counter cacheErrors;

    private final Counter cacheEvictions;

    private final Counter clickOutboxRecovered;

    private final Counter clickOutboxFailed;

    private final Timer dashboardBuilderTimer;

    public AnalyticsMetrics(
            MeterRegistry meterRegistry
    ) {

        this.cacheHits =
                Counter
                        .builder(
                                "analytics.dashboard.cache.hit"
                        )
                        .description(
                            "Number of analytics dashboard cache hits"
                        )
                        .register(meterRegistry);

        this.cacheMisses =
                Counter
                        .builder(
                        "analytics.dashboard.cache.miss"
                        )
                        .description(
                                "Number of analytics dashboard cache misses"
                        )
                        .register(meterRegistry);

        this.cacheErrors =
                Counter
                        .builder(
                                "analytics.dashboard.cache.error"
                        )
                        .description(
                                "Number of analytics dashboard cache errors"
                        )
                        .register(meterRegistry);

        this.cacheEvictions =
                Counter
                        .builder(
                                "analytics.dashboard.cache.eviction"
                        )
                        .description(
                                "Number of analytics dashboard cache evictions"
                        )
                        .register(meterRegistry);

        this.clickOutboxRecovered =
                Counter
                        .builder(
                                "analytics.click.outbox.recovered"
                        )
                        .description(
                                "Number of click events successfully recovered from the PostgreSQL outbox"
                        )
                        .register(meterRegistry);

        this.clickOutboxFailed =
                Counter
                        .builder(
                                "analytics.click.outbox.failed"
                        )
                        .description(
                                "Number of click events that failed during outbox recovery"
                        )
                        .register(meterRegistry);

        this.dashboardBuilderTimer =
                Timer
                        .builder(
                                "analytics.dashboard.build"
                        )
                        .description(
                                "Time required to build analytics dashboard"
                        )
                        .publishPercentiles(
                                0.50,
                                0.95,
                                0.99
                        )
                        .register(meterRegistry);
    }

    public void recordCacheHit() {

        cacheHits.increment();
    }

    public void recordCacheMiss() {

        cacheMisses.increment();
    }

    public void recordCacheError() {

        cacheErrors.increment();
    }

    public void recordCacheEviction() {

        cacheEvictions.increment();
    }

    public void recordClickOutboxRecovered() {

        clickOutboxRecovered.increment();
    }

    public  void recordClickOutboxFailed() {

        clickOutboxFailed.increment();
    }

    public Timer dashboardBuilderTimer() {

        return dashboardBuilderTimer;
    }
}
