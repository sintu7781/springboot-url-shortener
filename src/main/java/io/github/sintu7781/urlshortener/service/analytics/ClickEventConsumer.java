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

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    private final ClickEventRetryService retryService;

    @Scheduled(fixedDelay = 100)
    public void consume() {

        for (int i = 0; i < 100; i++) {

            String eventJson = redisTemplate.opsForList()
                    .rightPopAndLeftPush(
                            ClickEventPublisher.CLICK_EVENT_STREAM,
                            ClickEventPublisher.CLICK_EVENT_GROUP
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

            retryService.retry(eventJson);

            removeProcessedEvent(eventJson);
        }
    }

    public void retry(String eventJson) {

        processEvent(eventJson);

    }

    private void removeProcessedEvent(String eventJson) {

        redisTemplate.opsForList()
                .remove(
                        ClickEventPublisher.CLICK_EVENT_GROUP,
                        1,
                        eventJson
                );
    }
}
