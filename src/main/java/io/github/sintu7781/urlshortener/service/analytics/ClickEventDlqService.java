package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqPageResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickEventDlqService {

    public static final String DEAD_LETTER_STREAM =
            "stream:click-events:dlq";

    public static final String REPLAY_KEY_PREFIX =
            "click-event:dlq:replayed:";

    private final StringRedisTemplate redisTemplate;

    public RecordId replay(String dlqRecordId) {

        String replayKey =
                REPLAY_KEY_PREFIX + dlqRecordId;

        Boolean alreadyReplayed =
                redisTemplate.hasKey(replayKey);

        if(Boolean.TRUE.equals(alreadyReplayed)) {

            throw new IllegalStateException(
                    "DLQ event has already been replayed: "
                            + dlqRecordId
            );
        }

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

        redisTemplate.opsForValue()
                        .set(
                                replayKey,
                                newRecordId.getValue()
                        );

        log.info(
                "Replayed DLQ event {} as new stream event {}",
                dlqRecordId,
                newRecordId
        );

        return newRecordId;
    }

    public ClickEventDlqPageResponse list(
            String cursor,
            int limit
    ) {

        if(limit < 1 || limit > 100) {

            throw new IllegalArgumentException(
                    "Limit must be between 1 and 100."
            );
        }

        Range<String> range;

        if(cursor == null || cursor.isBlank()) {

            range = Range.unbounded();

        } else {

            range = Range.leftUnbounded(
                    Range.Bound.exclusive(cursor)
            );
        }

        List<MapRecord<String, Object, Object>> records =
                redisTemplate.opsForStream()
                .reverseRange(
                        DEAD_LETTER_STREAM,
                        range,
                        Limit.limit().count(limit + 1)
                );

        boolean hasNext =
                records.size() > limit;

        List<MapRecord<String, Object, Object>> page =
                hasNext
                        ? records.subList(0, limit)
                        : records;

        String nextCursor =
                hasNext
                        ? page.getLast()
                            .getId()
                            .getValue()
                        : null;

        List<ClickEventDlqResponse> events =
                page.stream()
                        .map(record ->
                                ClickEventDlqResponse.builder()
                                        .recordId(
                                                record.getId()
                                                        .getValue()
                                        )
                                        .event(
                                                value(record, "event")
                                        )
                                        .originalId(
                                                value(record, "originalId")
                                        )
                                        .deliveryCount(
                                                value(record, "deliveryCount")
                                        )
                                        .reason(
                                                value(record, "reason")
                                        )
                                        .build()
                        )
                        .toList();

        return ClickEventDlqPageResponse.builder()
                .events(events)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
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

    private static String value(
            MapRecord<String, Object, Object> record,
            String key
    ) {

        Object value =
                record.getValue().get(key);

        return value == null
                ? null
                : value.toString();
    }
}
