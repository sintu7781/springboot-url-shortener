package io.github.sintu7781.urlshortener.scheduler;

import io.github.sintu7781.urlshortener.service.analytics.ClickCountSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ClickCountSyncScheduler {

    private static final String CLICK_KEY_PREFIX = "clicks:";

    private final StringRedisTemplate redisTemplate;

    private final ClickCountSyncService clickCountSyncService;

    @Scheduled(fixedDelay = 60_000)
    public void syncClickCounts() {

        Set<String> keys = redisTemplate.keys(
                CLICK_KEY_PREFIX + "*"
        );

        if(keys == null || keys.isEmpty()) {
            return;
        }

        for(String key : keys) {

            String shortCode = key.substring(
                    CLICK_KEY_PREFIX.length()
            );

            clickCountSyncService.sync(shortCode);
        }
    }
}
