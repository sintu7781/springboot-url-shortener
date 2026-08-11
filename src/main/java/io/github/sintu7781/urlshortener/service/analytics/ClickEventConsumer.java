package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.config.RedisStreamConfig;
import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final RedisStreamConfig redisStreamConfig;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    private Subscription subscription;

    @PostConstruct
    public void start() {

        StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions<
                    String,
                    MapRecord<String, String, String>
                    > options =
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        StreamMessageListenerContainer<
                String,
                MapRecord<String, String, String>
                > container =
                StreamMessageListenerContainer.create(
                        redisTemplate.getRequiredConnectionFactory(),
                        options
                );

        subscription = container.receive(
                Consumer.from(
                        ClickEventPublisher.CLICK_EVENT_GROUP,
                        redisStreamConfig.consumerName()
                ),
                StreamOffset.create(
                        ClickEventPublisher.CLICK_EVENT_STREAM,
                        ReadOffset.lastConsumed()
                ),
                this::processRecord
        );

        container.start();
    }

    private void processRecord(
            MapRecord<String, String, String> record
    ) {

        String eventJson = record.getValue()
                .get("event");

        if(eventJson == null) {

            acknowledge(record);

            return;
        }

        try {

            ClickEvent event =
                    objectMapper.readValue(
                        eventJson,
                        ClickEvent.class
            );

            clickEventProcessor.process(event);

            acknowledge(record);

        } catch (Exception ex) {

            log.error(
                    "Failed to process click event: {}",
                    record.getId(),
                    ex
            );
        }
    }

    private void acknowledge(
            MapRecord<String, String, String> record
    ) {

        redisTemplate.opsForStream()
                .acknowledge(
                        ClickEventPublisher.CLICK_EVENT_GROUP,
                        record
                );
    }

    @PreDestroy
    public void stop() {

        if(subscription != null) {

            subscription.cancel();
        }
    }
}
