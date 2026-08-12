package io.github.sintu7781.urlshortener.controller.admin;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqPageResponse;
import io.github.sintu7781.urlshortener.dto.response.ClickEventDlqReplayResponse;
import io.github.sintu7781.urlshortener.service.analytics.ClickEventDlqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/analytics/dlq")
@RequiredArgsConstructor
public class ClickEventDlqController {

    private final ClickEventDlqService clickEventDlqService;

    @GetMapping
    public ResponseEntity<ApiResponse<ClickEventDlqPageResponse>> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int limit
    ) {

        ClickEventDlqPageResponse result =
                clickEventDlqService.list(
                        cursor,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<ClickEventDlqPageResponse>builder()
                                .success(true)
                                .message("DLQ events retrieved successfully.")
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @PostMapping("/{recordId}/replay")
    public ResponseEntity<ApiResponse<ClickEventDlqReplayResponse>> replay(
            @PathVariable String recordId
    ) {

        ClickEventDlqReplayResponse result =
                clickEventDlqService.replay(
                        recordId
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<ClickEventDlqReplayResponse>builder()
                                .success(true)
                                .message("Click event replayed successfully.")
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }
}
