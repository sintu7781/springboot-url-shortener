package io.github.sintu7781.urlshortener.scheduler;

import io.github.sintu7781.urlshortener.service.analytics.ClickCountSyncService;
import io.github.sintu7781.urlshortener.service.analytics.ClickCounterService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
    @SchedulerLock(
            name = "clickCountSync",
            lockAtMostFor = "5m",
            lockAtLeastFor = "30s"
    )
    public void syncClickCounts() {

        ScanOptions options =
                ScanOptions.scanOptions()
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

                boolean synced =
                        clickCountSyncService.sync(
                                shortCode,
                                rotatedKey
                        );

                if(synced) {
                    clickCounterService.deleteKey(rotatedKey);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    @SchedulerLock(
            name = "clickCountSyncRecovery",
            lockAtMostFor = "5m",
            lockAtLeastFor = "30s"
    )
    public void recoverRotatedBatches() {

        ScanOptions options =
                ScanOptions.scanOptions()
                        .match("click:*:sync:*")
                        .count(100)
                        .build();

        try (Cursor<String> cursor =
                redisTemplate.scan(options)) {

            while (cursor.hasNext()) {

                String rotatedKey = cursor.next();

                String shortCode = extractShortCode(
                        rotatedKey
                );

                if(shortCode == null) {
                    continue;
                }

                boolean synced =
                        clickCountSyncService.sync(
                                shortCode,
                                rotatedKey
                        );

                if(synced) {

                    clickCounterService.deleteKey(rotatedKey);
                }
            }
        }
    }

    private String extractShortCode(
            String rotatedKey
    ) {

        String prefix = "click:";

        if(!rotatedKey.startsWith(prefix)) {
            return null;
        }

        String value =
                rotatedKey.substring(prefix.length());

        int syncIndex =
                value.indexOf(":sync:");

        if(syncIndex <= 0) {
            return null;
        }

        return value.substring(0, syncIndex);
    }
}
