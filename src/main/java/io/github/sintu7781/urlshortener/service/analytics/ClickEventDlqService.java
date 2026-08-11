package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqPageResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqReplayResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqResponse;
import io.github.sintu7781.urlshortener.dto.response.ReplayAudit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class ClickEventDlqService {

    public static final String DEAD_LETTER_STREAM =
            "stream:click-events:dlq";

    public static final String REPLAY_KEY_PREFIX =
            "click-event:dlq:replayed:";

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<String> replayScript;

    private final ObjectMapper objectMapper;

    public ClickEventDlqService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {

        this.redisTemplate = redisTemplate;

        this.objectMapper = objectMapper;

        try {

            ClassPathResource resource =
                    new ClassPathResource(
                            "script/replay-click-event.lua"
                    );

            String script =
                    new String(
                            resource.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            this.replayScript =
                    new DefaultRedisScript<>(
                            script,
                            String.class
                    );
        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to load DLQ replay Lua script.",
                    ex
            );
        }
    }

    public ClickEventDlqReplayResponse replay(String dlqRecordId) {

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

        String replayKey =
                REPLAY_KEY_PREFIX + dlqRecordId;

        String result =
                redisTemplate.execute(
                        replayScript,
                        List.of(
                                replayKey,
                                ClickEventPublisher.CLICK_EVENT_STREAM
                        ),
                        eventValue.toString(),
                        "100000"
                );

        if(result.startsWith("ALREADY_REPLAYED:")) {

            String existingAudit =
                    result.substring(
                            "ALREADY_REPLAYED:".length()
                    );

            throw new IllegalStateException(
                    "DLQ event has already been replayed. "
                            + "Original DLQ record: "
                            + dlqRecordId
                            + ", replay: "
                            + existingAudit
            );
        }

        if(!result.startsWith("REPLAYED:")) {

            throw new IllegalStateException(
                    "Unexpected replay result: "
                            + result
            );
        }

        String auditJson =
                result.substring("REPLAYED:".length());

        try {

            ReplayAudit audit;

            try {

                audit = objectMapper.readValue(
                        auditJson,
                        ReplayAudit.class
                );

            } catch (Exception ex) {

                throw new IllegalStateException(
                        "Failed to parse DLQ replay result.",
                        ex
                );
            }

            if(audit.newStreamId() == null || audit.newStreamId().isEmpty()) {

                throw new IllegalStateException(
                        "Replay result does not contain newStreamId."
                );
            }

            if(audit.replayedAt() == null || audit.replayedAt().isEmpty()) {

                throw new  IllegalStateException(
                        "Replay result does not contain replayedAt."
                );
            }

            log.info(
                    "Replayed DLQ event {} as new stream event {}",
                    dlqRecordId,
                    audit.newStreamId()
            );

            return ClickEventDlqReplayResponse.builder()
                    .dlqRecordId(dlqRecordId)
                    .newStreamId(audit.newStreamId())
                    .replayedAt(audit.replayedAt())
                    .build();

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to parse DLQ replay result.",
                    ex
            );
        }
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
