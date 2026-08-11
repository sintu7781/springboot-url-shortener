package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventRecoveryService {

    private static final String RECOVERY_CONSUMER =
            "click-recovery";

    private static final String DEAD_LETTER_STREAM =
            "stream:click-events:dlq";

    private static final Duration MIN_IDLE_TIME =
            Duration.ofSeconds(60);

    private static final long BATCH_SIZE = 100L;

    private static final long MAX_RETRY_ATTEMPTS = 5L;

    private static final long MAX_DLQ_LENGTH = 10_000L;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    @Scheduled(fixedDelay = 30_000)
    @SchedulerLock(
            name = "clickEventRecovery",
            lockAtMostFor = "2m",
            lockAtLeastFor = "10s"
    )
    public void recoveryPendingEvents() {

        try {

            PendingMessages pending =
                    redisTemplate.opsForStream()
                            .pending(
                                    ClickEventPublisher.CLICK_EVENT_STREAM,
                                    ClickEventPublisher.CLICK_EVENT_GROUP,
                                    Range.unbounded(),
                                    BATCH_SIZE,
                                    MIN_IDLE_TIME
                            );

            if(pending == null || pending.isEmpty()) {
                return;
            }

            List<RecordId> recordIdsToClaim =
                    new ArrayList<>();

            for(int i = 0; i < pending.size(); i++) {

                var message = pending.get(i);

                long deliveryCount =
                        message.getTotalDeliveryCount();

                if(deliveryCount >= MAX_RETRY_ATTEMPTS) {

                    moveToDeadLetterQueue(
                            message.getId(),
                            deliveryCount
                    );

                } else {

                    recordIdsToClaim.add(
                            message.getId()
                    );
                }
            }

            if(recordIdsToClaim.isEmpty()) {
                return;
            }

            List<MapRecord<String, Object, Object>> records =
                    redisTemplate.opsForStream()
                            .claim(
                                    ClickEventPublisher.CLICK_EVENT_STREAM,
                                    ClickEventPublisher.CLICK_EVENT_GROUP,
                                    RECOVERY_CONSUMER,
                                    MIN_IDLE_TIME,
                                    recordIdsToClaim.toArray(
                                            RecordId[]::new
                                    )
                            );

            if(records == null || records.isEmpty()) {
                return;
            }

            for(MapRecord<String, Object, Object> record : records) {

                processRecoveredEvent(record);
            }

        } catch (Exception ex) {

            log.error(
                    "Failed to recover pending click events",
                    ex
            );
        }
    }

    private void processRecoveredEvent(
            MapRecord<String, Object, Object> record
    ) {

        Object eventValue = record.getValue()
                .get("event");

        if(eventValue == null) {

            acknowledge(record);

            return;
        }

        String eventJson =
                eventValue.toString();

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
                    "Failed to process recovered event {}",
                    record.getId(),
                    ex
            );
        }
    }

    private void moveToDeadLetterQueue(
            RecordId recordId,
            long deliveryCount
    ) {

        List<MapRecord<String, Object, Object>> records =
                redisTemplate.opsForStream()
                        .range(
                                ClickEventPublisher.CLICK_EVENT_STREAM,
                                Range.closed(
                                        recordId.toString(),
                                        recordId.toString()
                                )
                        );

        if(records == null || records.isEmpty()) {

            log.warn(
                    "Could not find pending event {} in stream",
                    recordId
            );

            return;
        }

        MapRecord<String, Object, Object> original =
                records.getFirst();

        Object eventValue =
                original.getValue().get("event");

        if(eventValue == null) {

            acknowledge(original);

            return;
        }

        redisTemplate.opsForStream()
                .add(
                        DEAD_LETTER_STREAM,
                        Map.of(
                                "event",
                                eventValue.toString(),
                                "originalId",
                                recordId.toString(),
                                "deliveryCount",
                                String.valueOf(deliveryCount),
                                "reason",
                                "MAX_RETRY_ATTEMPTS_EXCEEDED"
                        ),
                        RedisStreamCommands.XAddOptions.trim(
                                RedisStreamCommands.TrimOptions
                                        .maxLen(MAX_DLQ_LENGTH)
                                        .approximate()
                        )
                );

        acknowledge(original);

        log.warn(
                "Moved click event {} to dead-letter stream after {} deliveries",
                        recordId,
                        deliveryCount
        );
    }

    private void acknowledge(
            MapRecord<String, Object, Object> record
    ) {

        redisTemplate.opsForStream()
                .acknowledge(
                        ClickEventPublisher.CLICK_EVENT_GROUP,
                        record
                );
    }
}
