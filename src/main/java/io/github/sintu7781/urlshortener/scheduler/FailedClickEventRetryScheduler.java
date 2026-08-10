package io.github.sintu7781.urlshortener.scheduler;

import io.github.sintu7781.urlshortener.service.analytics.ClickEventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FailedClickEventRetryScheduler {

    private static final String PROCESSING_QUEUE =
            "processing:click-events";

    private final StringRedisTemplate redisTemplate;

    private final ClickEventConsumer clickEventConsumer;

    @Scheduled(fixedDelay = 10_000)
    public void retryFailedEvents() {

        for(int i = 0; i < 100; i++) {

            String eventJson = redisTemplate.opsForList()
                    .leftPop(PROCESSING_QUEUE);

            if(eventJson == null) {
                return;
            }

            clickEventConsumer.retry(eventJson);
        }
    }
}
