package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsDashboardResponse;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsCacheService {

    private static final String CACHE_PREFIX =
            "analytics:dashboard:v1:";

    private static final String LOCK_PREFIX =
            "analytics:dashboard:lock:v1:";

    private static final Duration CACHE_TTL =
            Duration.ofSeconds(60);

    private static final Duration LOCK_TTL =
            Duration.ofSeconds(10);

    private static final Duration LOCK_WAIT =
            Duration.ofMillis(100);

    private static final int MAX_LOCK_ATTEMPTS = 20;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final AnalyticsMetrics analyticsMetrics;

    public Optional<UrlAnalyticsDashboardResponse> getDashboard(
            String shortCode,
            Instant from,
            Instant to,
            String timezone,
            int limit
    ) {

        String key = buildKey(
                shortCode,
                from,
                to,
                timezone,
                limit
        );

        String cachedValue =
                redisTemplate.opsForValue().get(key);

        if(cachedValue == null || cachedValue.isBlank()) {

            analyticsMetrics.recordCacheMiss();

            log.debug(
                    "Analytics dashboard cache miss. key={}",
                    key
            );

            return Optional.empty();
        }

        try {

            analyticsMetrics.recordCacheHit();

            log.debug(
                    "Analytics dashboard cache hit. key={}",
                    key
            );

            return Optional.of(
                    objectMapper.readValue(
                            cachedValue,
                            UrlAnalyticsDashboardResponse.class
                    )
            );
        } catch (Exception ex) {

            analyticsMetrics.recordCacheError();

            log.warn(
                    "Invalid analytics cache entry. key={}",
                    key,
                    ex
            );

            redisTemplate.delete(key);

            return Optional.empty();
        }
    }

    public void putDashboard(
            String shortCode,
            Instant from,
            Instant to,
            String timezone,
            int limit,
            UrlAnalyticsDashboardResponse response
    ) {

        String key = buildKey(
                shortCode,
                from,
                to,
                timezone,
                limit
        );

        try {

            String value =
                    objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(
                    key,
                    value,
                    CACHE_TTL
            );
        } catch (Exception ex) {

            analyticsMetrics.recordCacheError();

            log.warn(
                    "Failed to cache analytics dashboard. shortCode={}, from={}, to={}, timezone={}, limit={}",
                    shortCode,
                    from,
                    to,
                    timezone,
                    limit,
                    ex
            );
        }
    }

    public void evictDashboard(String shortCode) {

        String pattern =
                CACHE_PREFIX
                        + shortCode
                        + ":*";

        try {

            Set<String> keys = new HashSet<>();

            try (Cursor<String> cursor =
                    redisTemplate.scan(
                            ScanOptions.scanOptions()
                                    .match(pattern)
                                    .count(100)
                                    .build()
                    )) {
                cursor.forEachRemaining(keys::add);
            }

            if(!keys.isEmpty()) {

                redisTemplate.delete(keys);

                analyticsMetrics.recordCacheEviction();
            }

        } catch (Exception ex) {

            log.warn(
                    "Failed to evict analytics dashboard cache. shortCode={}",
                    shortCode,
                    ex
            );
        }
    }

    public Optional<UrlAnalyticsDashboardResponse> getDashboardWithLock(
            String shortCode,
            Instant from,
            Instant to,
            String timezone,
            int limit,
            Supplier<UrlAnalyticsDashboardResponse> loader
    ) {

        Optional<UrlAnalyticsDashboardResponse> cached =
                getDashboard(
                        shortCode,
                        from,
                        to,
                        timezone,
                        limit
                );

        if(cached.isPresent()) {

            return  cached;
        }

        String lockKey = buildLockKey(
                shortCode,
                from,
                to,
                timezone,
                limit
        );

        String lockToken =
                UUID.randomUUID().toString();

        boolean acquired = false;

        try {

            acquired = acquireLock(
                    lockKey,
                    lockToken
            );

            if(acquired) {

                cached =
                        getDashboard(
                                shortCode,
                                from,
                                to,
                                timezone,
                                limit
                        );

                if(cached.isPresent()) {

                    return cached;
                }

                UrlAnalyticsDashboardResponse result =
                        loadAndMeasure(loader);

                putDashboard(
                        shortCode,
                        from,
                        to,
                        timezone,
                        limit,
                        result
                );

                return Optional.of(result);
            }

            for (int attempt = 0;
                 attempt < MAX_LOCK_ATTEMPTS;
                 attempt++) {

                Thread.sleep(
                        LOCK_WAIT.toMillis()
                );

                cached =
                        getDashboard(
                                shortCode,
                                from,
                                to,
                                timezone,
                                limit
                        );

                if(cached.isPresent()) {

                    return cached;
                }
            }

            UrlAnalyticsDashboardResponse result =
                    loadAndMeasure(loader);

            putDashboard(
                    shortCode,
                    from,
                    to,
                    timezone,
                    limit,
                    result
            );

            return Optional.of(result);

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while waiting for analytics cache.",
                    ex
            );
        } finally {

            if(acquired) {

                releaseLock(
                        lockKey,
                        lockToken
                );
            }
        }
    }

    private UrlAnalyticsDashboardResponse loadAndMeasure(
            Supplier<UrlAnalyticsDashboardResponse> loader
    ) {

        Timer timer =
                analyticsMetrics.dashboardBuilderTimer();

        return timer.record(loader);
    }

    private boolean acquireLock(
            String lockKey,
            String lockToken
    ) {

        Boolean acquired =
                redisTemplate.opsForValue().setIfAbsent(
                        lockKey,
                        lockToken,
                        LOCK_TTL
                );

        return Boolean.TRUE.equals(acquired);
    }

    private void releaseLock(
            String lockKey,
            String lockToken
    ) {

        String script = """
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                else
                    return 0
                end
                """;

        redisTemplate.execute(
                RedisScript.of(script, Long.class),
                List.of(lockKey),
                lockToken
        );
    }

    private String buildKey(
            String shortCode,
            Instant from,
            Instant to,
            String timezone,
            int limit
    ) {

        return CACHE_PREFIX
                + shortCode
                + ":"
                + from
                + ":"
                + to
                + ":"
                + timezone
                + ":"
                + limit;
    }

    private String buildLockKey(
            String shortCode,
            Instant from,
            Instant to,
            String timezone,
            int limit
    ) {

        return LOCK_PREFIX
                + shortCode
                + ":"
                + from
                + ":"
                + to
                + ":"
                + timezone
                + ":"
                + limit;
    }
}
