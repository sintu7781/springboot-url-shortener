package io.github.sintu7781.urlshortener.controller.admin;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.dto.response.*;
import io.github.sintu7781.urlshortener.service.analytics.AnalyticsContext;
import io.github.sintu7781.urlshortener.service.analytics.AnalyticsService;
import io.github.sintu7781.urlshortener.service.analytics.AnalyticsTimeZoneService;
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
    private final AnalyticsTimeZoneService analyticsTimeZoneService;

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

    @GetMapping("/{shortCode}/timeseries")
    public ResponseEntity<ApiResponse<UrlAnalyticsTimeSeriesResponse>> getUrlAnalyticsTimeSeries(
            @PathVariable String shortCode,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {

        AnalyticsContext context =
                analyticsTimeZoneService.createContext(
                        from,
                        to,
                        timezone
                );

        UrlAnalyticsTimeSeriesResponse result =
                analyticsService.getUrlAnalyticsTimeSeries(
                        shortCode,
                        context
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsTimeSeriesResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics time series fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/timeseries/hourly")
    public ResponseEntity<ApiResponse<UrlAnalyticsHourlyResponse>> getUrlAnalyticsHourly(
            @PathVariable String shortCode,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {

        AnalyticsContext context =
                analyticsTimeZoneService.createContext(
                        from,
                        to,
                        timezone
                );

        UrlAnalyticsHourlyResponse result =
                analyticsService.getUrlAnalyticsHourly(
                        shortCode,
                        context
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsHourlyResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics hourly time series fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/referrers")
    public ResponseEntity<ApiResponse<UrlAnalyticsReferrerResponse>> getUrlAnalyticsReferrers(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "10") int limit
    ) {

        UrlAnalyticsReferrerResponse result =
                analyticsService.getUrlAnalyticsReferrers(
                        shortCode,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsReferrerResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics referrers fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("{shortCode}/user-agents")
    public  ResponseEntity<ApiResponse<UrlAnalyticsUserAgentResponse>> getUrlAnalyticsUserAgents(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "10") int limit
    ) {

        UrlAnalyticsUserAgentResponse result =
                analyticsService.getUrlAnalyticsUserAgents(
                        shortCode,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsUserAgentResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics user agents fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/browsers")
    public  ResponseEntity<ApiResponse<UrlAnalyticsBrowserResponse>> getUrlAnalyticsBrowsers(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "10") int limit
    ) {

        UrlAnalyticsBrowserResponse result =
                analyticsService.getUrlAnalyticsBrowsers(
                        shortCode,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsBrowserResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics browsers fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/operating-systems")
    public  ResponseEntity<ApiResponse<UrlAnalyticsOperatingSystemResponse>> getUrlAnalyticsOperatingSystems(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "10") int limit
    ) {

        UrlAnalyticsOperatingSystemResponse result =
                analyticsService.getUrlAnalyticsOperatingSystems(
                        shortCode,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsOperatingSystemResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics operating systems fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/devices")
    public  ResponseEntity<ApiResponse<UrlAnalyticsDeviceResponse>> getUrlAnalyticsDevices(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "10") int limit
    ) {

        UrlAnalyticsDeviceResponse result =
                analyticsService.getUrlAnalyticsDevices(
                        shortCode,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsDeviceResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics devices fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }

    @GetMapping("/{shortCode}/dashboard")
    public  ResponseEntity<ApiResponse<UrlAnalyticsDashboardResponse>> getDashboard(
            @PathVariable String shortCode,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {

        AnalyticsContext context =
                analyticsTimeZoneService.createContext(
                        from,
                        to,
                        timezone
                );

        UrlAnalyticsDashboardResponse result =
                analyticsService.getDashboard(
                        shortCode,
                        context,
                        limit
                );

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<UrlAnalyticsDashboardResponse>builder()
                                .success(true)
                                .message(
                                        "URL analytics dashboard fetched successfully."
                                )
                                .data(result)
                                .timestamp(Instant.now())
                                .build()
                );
    }
}
