package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Range;
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

@Service
@RequiredArgsConstructor
public class ClickEventRecoveryService {

    private static final String RECOVERY_CONSUMER =
            "click-recovery";

    private static final Duration MIN_IDLE_TIME =
            Duration.ofSeconds(60);

    private static final long BATCH_SIZE = 100;

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final ClickEventProcessor clickEventProcessor;

    @Scheduled(fixedDelay = 30_000)
    @SchedulerLock(
            name = "clickEventRecovery",
            lockAtMostFor = "5m",
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

            List<RecordId> recordIds = new ArrayList<>();

            for(int i = 0; i < pending.size(); i++) {

                recordIds.add(pending.get(i).getId());
            }

            if(recordIds.isEmpty()) {
                return;
            }

            List<MapRecord<String, Object, Object>> records =
                    redisTemplate.opsForStream()
                            .claim(
                                    ClickEventPublisher.CLICK_EVENT_STREAM,
                                    ClickEventPublisher.CLICK_EVENT_GROUP,
                                    RECOVERY_CONSUMER,
                                    MIN_IDLE_TIME,
                                    recordIds.toArray(
                                            RecordId[]::new
                                    )
                            );

            for(MapRecord<String, Object, Object> record : records) {

                process(record);
            }

        } catch (Exception ex) {

            System.out.println(
                    "Failed to recover pending click events: "
                    + ex.getMessage()
            );
        }
    }

    private void process(
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

            System.out.println(
                    "Failed to process recovered event "
                    + record.getId()
                    + ": "
                    + ex.getMessage()
            );
        }
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
