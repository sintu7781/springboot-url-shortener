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

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    @Scheduled(fixedDelay = 100)
    public void consume() {

        for (int i = 0; i < 100; i++) {

            String eventJson = redisTemplate.opsForList()
                    .leftPop(CLICK_EVENT_QUEUE);

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

        } catch (Exception ex) {

            System.err.println(
                    "Failed to process click event: "
                    + ex.getMessage()
            );
        }
    }
}
