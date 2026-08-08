package io.github.sintu7781.urlshortener.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private static final String KEY_PREFIX = "url:";

    private final StringRedisTemplate redisTemplate;

    public String get(String shortCode) {

        return redisTemplate.opsForValue()
                .get(buildKey(shortCode));
    }

    public void put(
            String shortCode,
            String originalUrl,
            Duration ttl
    ) {

        if(ttl == null || ttl.isZero() || ttl.isNegative()) {

            redisTemplate.opsForValue()
                    .set(buildKey(shortCode), originalUrl);

            return;
        }

        redisTemplate.opsForValue()
                .set(
                        buildKey(shortCode),
                        originalUrl,
                        ttl
                );
    }

    public void delete(String shortCode) {

        redisTemplate.delete(buildKey(shortCode));
    }

    private String buildKey(String shortCode) {

        return KEY_PREFIX + shortCode;
    }
}
