package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ClickEventRetryService {

    private static final String CLICK_EVENT_QUEUE =
            "queue:click-events";

    private static final String RETRY_PREFIX =
            "click-event:retry:";

    private static final String DEAD_LETTER_QUEUE =
            "dead:click-events";

    private static final int MAX_RETRIES = 3;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public void retry(String eventJson) {

        try {

            ClickEvent event = objectMapper.readValue(
                    eventJson,
                    ClickEvent.class
            );

            String retryKey = RETRY_PREFIX + event.eventId();

            Long retryCount = redisTemplate.opsForValue()
                    .increment(retryKey);

            if(retryCount == null) {
                return;
            }

            if(retryCount >= MAX_RETRIES) {

                redisTemplate.opsForList()
                        .rightPush(
                                DEAD_LETTER_QUEUE,
                                eventJson
                        );

                redisTemplate.delete(retryKey);

                return;
            }

            redisTemplate.opsForList()
                    .rightPush(
                            CLICK_EVENT_QUEUE,
                            eventJson
                    );
        } catch (Exception ex) {

            redisTemplate.opsForList()
                    .rightPush(
                            DEAD_LETTER_QUEUE,
                            eventJson
                    );
        }
    }
}
