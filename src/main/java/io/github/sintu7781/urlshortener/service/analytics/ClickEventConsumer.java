package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ClickEventConsumer {

    private static final String CLICK_EVENT_QUEUE =
            "queue:click-events";

    private static final String PROCESSING_QUEUE =
            "processing:click-events";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    @Scheduled(fixedDelay = 100)
    public void consume() {

        for (int i = 0; i < 100; i++) {

            String eventJson = redisTemplate.opsForList()
                    .rightPopAndLeftPush(
                            CLICK_EVENT_QUEUE,
                            PROCESSING_QUEUE
                    );

            if (eventJson == null) {
                return;
            }

            processEvent(eventJson);

        }
    }

    private void processEvent(String eventJson) {

        try {

            ClickEvent event = objectMapper.readValue(
                    eventJson,
                    ClickEvent.class
            );

            clickEventProcessor.process(event);

            removeProcessedEvent(eventJson);

        } catch (Exception ex) {

            System.err.println(
                    "Failed to process click event: "
                    + ex.getMessage()
            );
        }
    }

    public void retry(String eventJson) {

        processEvent(eventJson);

    }

    private void removeProcessedEvent(String eventJson) {

        redisTemplate.opsForList()
                .remove(
                        PROCESSING_QUEUE,
                        1,
                        eventJson
                );
    }
}
