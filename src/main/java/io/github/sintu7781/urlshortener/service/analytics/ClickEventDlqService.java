package io.github.sintu7781.urlshortener.service.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventDlqService {

    public static final String DEAD_LETTER_STREAM =
            "stream:click-events:dlq";

    private final StringRedisTemplate redisTemplate;

    public RecordId replay(String dlqRecordId) {

        MapRecord<String, Object, Object> record =
                findRecord(dlqRecordId);

        if(record == null) {

            throw new IllegalArgumentException(
                    "DLQ event not found: " + dlqRecordId
            );
        }

        Object eventValue =
                record.getValue().get("event");

        if(eventValue == null) {

            throw new IllegalArgumentException(
                    "DLQ event does not contain event payload: "
                            + dlqRecordId
            );
        }

        RecordId newRecordId =
                redisTemplate.opsForStream()
                        .add(
                                ClickEventPublisher.CLICK_EVENT_STREAM,
                                Map.of(
                                        "event",
                                        eventValue.toString()
                                ),
                                RedisStreamCommands.XAddOptions.trim(
                                        RedisStreamCommands.TrimOptions
                                                .maxLen(100_000L)
                                                .approximate()
                                )
                        );

        if(newRecordId == null) {

            throw new IllegalArgumentException(
                    "Failed to replay DLQ event: "
                            + dlqRecordId
            );
        }

        log.info(
                "Replayed DLQ event {} as new stream event {}",
                dlqRecordId,
                newRecordId
        );

        return newRecordId;
    }

    private MapRecord<String, Object, Object> findRecord(
            String dlqRecord
    ) {

        return redisTemplate.opsForStream()
                .range(
                        DEAD_LETTER_STREAM,
                        Range.closed(
                                dlqRecord,
                                dlqRecord
                        )
                )
                .stream()
                .findFirst()
                .orElse(null);
    }
}
