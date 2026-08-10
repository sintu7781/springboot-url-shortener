package io.github.sintu7781.urlshortener.service.impl;

import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface UrlService {

    UrlResponse createShortUrl(CreateShortUrlRequest request);

    String getOriginalUrl(
            String shortCode,
            HttpServletRequest request
    );
}
