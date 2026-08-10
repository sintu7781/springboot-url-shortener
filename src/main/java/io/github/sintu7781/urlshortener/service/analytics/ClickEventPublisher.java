package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ClickEventPublisher {

    private static final String CLICK_EVENT_QUEUE =
            "queue:click-events";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public void publish(ClickEvent event) {

        try {

            String json = objectMapper.writeValueAsString(event);

            redisTemplate.opsForList()
                    .rightPush(CLICK_EVENT_QUEUE, json);

        } catch (JacksonException ex) {

            throw new IllegalStateException(
                    "Failed to publish click event.",
                    ex
            );
        }
    }
}
