package io.github.sintu7781.urlshortener.controller;

import io.github.sintu7781.urlshortener.common.response.ApiResponse;
import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.service.impl.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<ApiResponse<UrlResponse>> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<UrlResponse>builder()
                                .success(true)
                                .message("Short URL created successfully.")
                                .data(urlService.createShortUrl(request))
                                .timestamp(Instant.now())
                                .build()
                );
    }
}
