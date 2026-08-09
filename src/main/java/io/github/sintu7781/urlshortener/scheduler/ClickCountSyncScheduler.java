package io.github.sintu7781.urlshortener.scheduler;

import io.github.sintu7781.urlshortener.service.analytics.ClickCountSyncService;
import io.github.sintu7781.urlshortener.service.analytics.ClickCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClickCountSyncScheduler {

    private static final String CLICK_KEY_PATTERN = "clicks:*";

    private final StringRedisTemplate redisTemplate;

    private final ClickCounterService clickCounterService;

    private final ClickCountSyncService clickCountSyncService;

    @Scheduled(fixedDelay = 60_000)
    public void syncClickCounts() {

        ScanOptions options = ScanOptions.scanOptions()
                .match(CLICK_KEY_PATTERN)
                .count(100)
                .build();

        try (Cursor<String> cursor =
                redisTemplate.scan(options)) {

            while (cursor.hasNext()) {

                String key = cursor.next();

                if(key.contains(":sync:")) {
                    continue;
                }

                String shortCode = key.substring(
                        "clicks:".length()
                );

                String rotatedKey =
                        clickCounterService.rotate(shortCode);

                if(rotatedKey == null) {
                    continue;
                }

                clickCountSyncService.sync(
                        shortCode,
                        rotatedKey
                );
            }
        }
    }
}
