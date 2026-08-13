package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsCacheService {

    private static final String CACHE_PREFIX =
            "analytics:dashboard:v1:";

    private static final Duration CACHE_TTL =
            Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

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

            log.debug(
                    "Analytics dashboard cache miss. key={}",
                    key
            );

            return Optional.empty();
        }

        try {

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

        var keys =
                redisTemplate.keys(pattern);

        if(keys != null && !keys.isEmpty()) {

            redisTemplate.delete(keys);
        }
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
}
