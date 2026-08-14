package io.github.sintu7781.urlshortener.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
            
            redis.call('RENAME', KEYS[1], newKey)
            
            return newKey
            """;

    private final StringRedisTemplate redisTemplate;

    public long increment(String shortCode) {

        try {

            Long count = redisTemplate.opsForValue()
                    .increment(buildKey(shortCode));

            return count != null ? count : 0L;
        } catch (Exception ex) {

            log.warn(
                    "Failed to increment click counter. shortCode={}",
                    shortCode,
                    ex
            );

            return 0L;
        }
    }

    public long get(String shortCode) {

        String value =
                redisTemplate.opsForValue()
                        .get(buildKey(shortCode));

        if (value == null) {
            return 0L;
        }

        return Long.parseLong(value);
    }

    public long getRotatedCount(String rotatedKey) {

        String value = redisTemplate.opsForValue()
                .get(rotatedKey);

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

    public void deleteKey(String key) {

        redisTemplate.delete(key);
    }

    private String buildKey(String shortCode) {

        return KEY_PREFIX + shortCode;
    }
}
