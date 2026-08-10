package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClickEventPublisher {

    public static final String CLICK_EVENT_STREAM =
            "stream:click-events";

    public static final String CLICK_EVENT_GROUP =
            "click-event-workers";

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public void publish(ClickEvent event) {

        try {

            String json = objectMapper.writeValueAsString(event);

            RecordId recordId = redisTemplate.opsForStream()
                            .add(
                                    CLICK_EVENT_STREAM,
                                    Map.of("event", json)
                            );

            if(recordId == null) {

                throw new IllegalStateException(
                        "Failed to publish click event."
                );
            }

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to publish click event.",
                    ex
            );
        }
    }
}
