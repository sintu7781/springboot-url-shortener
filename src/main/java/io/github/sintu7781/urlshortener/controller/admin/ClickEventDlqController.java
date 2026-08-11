package io.github.sintu7781.urlshortener.controller.admin;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.service.analytics.ClickEventDlqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/analytics/dlq")
@RequiredArgsConstructor
public class ClickEventDlqController {

    private final ClickEventDlqService clickEventDlqService;

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
}
