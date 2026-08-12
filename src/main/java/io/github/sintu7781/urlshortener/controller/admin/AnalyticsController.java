package io.github.sintu7781.urlshortener.controller.admin;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsRangeResponse;
import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsResponse;
import io.github.sintu7781.urlshortener.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponse>> getUrlAnalytics(
            @PathVariable String shortCode
    ) {

        UrlAnalyticsResponse result =
                analyticsService.getUrlAnalytics(shortCode);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsResponse>builder()
                                .success(true)
                                .message("URL analytics fetched successfully.")
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/range")
    public ResponseEntity<ApiResponse<UrlAnalyticsRangeResponse>> getUrlAnalyticsRange(
            @PathVariable String shortCode,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {

        UrlAnalyticsRangeResponse result =
                analyticsService.getUrlAnalytics(
                        shortCode,
                        from,
                        to
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsRangeResponse>builder()
                                .success(true)
                                .message("URL analytics range fetched successfully.")
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }
}
