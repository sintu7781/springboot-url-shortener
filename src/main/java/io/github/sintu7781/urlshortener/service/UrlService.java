package io.github.sintu7781.urlshortener.service;

import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;

public interface UrlService {

    UrlResponse createShortUrl(CreateShortUrlRequest request);
}
