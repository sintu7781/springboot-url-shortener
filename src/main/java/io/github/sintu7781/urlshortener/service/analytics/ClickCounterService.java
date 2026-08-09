package io.github.sintu7781.urlshortener.service.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClickCounterService {

    private static final String KEY_PREFIX = "clicks:";

    private static final String ROTATE_SCRIPT = """
            local value = redis.call('GET', KEYS[1])
            
            if not value then
                return nil
            end
            
            local newKey = KEYS[1] .. ':sync:' .. ARGV[1]
            
            redis.call('RENAME', KEYS[1], newKeys)
            
            return newKey
            """;

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

    public String rotate(String shortCode) {

        String currentKey = buildKey(shortCode);

        String timestamp = String.valueOf(
                System.currentTimeMillis()
        );

        return redisTemplate.execute(
                new DefaultRedisScript<>(
                        ROTATE_SCRIPT,
                        String.class
                ),
                List.of(currentKey),
                timestamp
        );
    }

    public long getAndDelete(String key) {
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
