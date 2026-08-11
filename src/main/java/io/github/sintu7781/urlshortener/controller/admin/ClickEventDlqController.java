package io.github.sintu7781.urlshortener.controller.admin;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqResponse;
import io.github.sintu7781.urlshortener.service.analytics.ClickEventDlqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics/dlq")
@RequiredArgsConstructor
public class ClickEventDlqController {

    private final ClickEventDlqService clickEventDlqService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClickEventDlqResponse>>> list() {

        List<ClickEventDlqResponse> events =
                clickEventDlqService.list()
                        .stream()
                        .map(record -> ClickEventDlqResponse.builder()
                                .recordId(record.getId().getValue())
                                .event(value(record, "event"))
                                .originalId(value(record, "originalId"))
                                .deliveryCount(value(record, "deliveryCount"))
                                .reason(value(record, "reason"))
                                .build())
                        .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<List<ClickEventDlqResponse>>builder()
                                .success(true)
                                .message("DLQ events retrieved successfully.")
                                .data(events)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @PostMapping("/{recordId}/replay")
    public ResponseEntity<ApiResponse<String>> replay(
            @PathVariable String recordId
    ) {

        String newRecordId =
                clickEventDlqService
                        .replay(recordId)
                        .getValue();

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<String>builder()
                                .success(true)
                                .message("Click event replayed successfully.")
                                .data(newRecordId)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    private static String value(
            MapRecord<String, Object, Object> record,
            String key
    ) {

        Object value = record.getValue().get(key);

        return value == null
                ? null
                : value.toString();
    }
}
