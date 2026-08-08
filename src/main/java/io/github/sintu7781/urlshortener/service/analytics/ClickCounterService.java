package io.github.sintu7781.urlshortener.service.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickCounterService {

    private static final String KEY_PREFIX = "clicks:";

    private final StringRedisTemplate redisTemplate;

    public long increment(String shortCode) {

        Long count = redisTemplate.opsForValue()
                .increment(buildKey(shortCode));

        return count != null ? count : 0L;
    }

    public long get(String shortCode) {

        String value = redisTemplate.opsForValue()
                .get(buildKey(shortCode));

        if(value == null) {
            return 0L;
        }

        return Long.parseLong(value);
    }

    public Long getAndReset(String shortCode) {

        String key = buildKey(shortCode);

        String value = redisTemplate.opsForValue()
                .getAndDelete(key);

        if(value == null) {
            return 0L;
        }

        return Long.parseLong(value);
    }

    public void delete(String shortCode) {

        redisTemplate.delete(buildKey(shortCode));
    }

    private String buildKey(String shortCode) {

        return KEY_PREFIX + shortCode;
    }
}
