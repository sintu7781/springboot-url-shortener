package io.github.sintu7781.urlshortener.service.impl;

import io.github.sintu7781.urlshortener.common.exception.*;
import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.mapper.UrlMapper;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
//import io.github.sintu7781.urlshortener.service.id.IdGenerator;
import io.github.sintu7781.urlshortener.common.util.Base62Generator;
import io.github.sintu7781.urlshortener.service.analytics.ClickCounterService;
import io.github.sintu7781.urlshortener.service.analytics.ClickEventPublisher;
import io.github.sintu7781.urlshortener.service.cache.UrlCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;

//    private final IdGenerator idGenerator;

    private final UrlMapper mapper;

    private final UrlCacheService urlCacheService;

    private final ClickCounterService clickCounterService;

    private final ClickEventPublisher clickEventPublisher;

    private void validateUrl(String url) {

        try {
            URI uri = URI.create(url);

            String schema = uri.getScheme();

            if(schema == null ||
            uri.getHost() == null ||
                    (!schema.equalsIgnoreCase("http")
                    && !schema.equalsIgnoreCase("https"))) {

                throw new InvalidUrlException(
                        "URL must be a valid HTTP or HTTPS URL."
                );
            }
        } catch (IllegalArgumentException ex) {

            throw new InvalidUrlException(
                    "Invalid URL format."
            );
        }
    }

    private void validateExpiration(LocalDateTime expiresAt) {

        if(expiresAt != null &&
                !expiresAt.isAfter(LocalDateTime.now())) {

            throw new InvalidExpirationException(
                    "Expiration time must be in the future."
            );
        }
    }

    private void cacheUrl(Url url) {

        if(url.getExpiresAt() == null) {

            urlCacheService.put(
                    url.getShortCode(),
                    url.getOriginalUrl(),
                    null
            );

            return;
        }

        Duration ttl = Duration.between(
                LocalDateTime.now(),
                url.getExpiresAt()
        );

        if(!ttl.isNegative() && !ttl.isZero()) {

            urlCacheService.put(
                    url.getShortCode(),
                    url.getOriginalUrl(),
                    ttl
            );
        }
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if(forwardedFor != null &&
            !forwardedFor.isBlank()) {

            return forwardedFor.split(",")[0].trim();

        }

        return request.getRemoteAddr();
    }

    private void recordClick(
            String shortCode,
            HttpServletRequest request
    ) {

        ClickEvent event = ClickEvent.builder()
                .shortCode(shortCode)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .referrer(request.getHeader("Referrer"))
                .clickedAt(Instant.now())
                .build();

        clickEventPublisher.publish(event);
    }

    @Transactional
    @Override
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {

        validateUrl(request.getUrl());

        validateExpiration(request.getExpiresAt());

        Url existing = urlRepository
                .findByOriginalUrl(request.getUrl())
                .orElse(null);

        if(existing != null) {
            return mapper.toResponse(existing);
        }

        long id = urlRepository.getNextId();
//        long id = idGenerator.nextId();

        String shortCode;

        if(request.getCustomAlias() != null &&
                !request.getCustomAlias().isBlank()) {

            if(urlRepository.existsByShortCode(request.getCustomAlias())) {

                throw new ShortCodeAlreadyExistsException(
                        "Custom alias is already in use."
                );
            }

            shortCode = request.getCustomAlias();

        } else {

            shortCode = Base62Generator.encode(id);

        }

        Url url = Url.builder()
                .id(id)
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .expiresAt(request.getExpiresAt())
                .build();

        Url savedUrl = urlRepository.save(url);

        cacheUrl(savedUrl);

        return mapper.toResponse(savedUrl);
    }

    @Transactional
    @Override
    public String getOriginalUrl(
            String shortCode,
            HttpServletRequest request
    ) {

        String cachedUrl = urlCacheService.get(shortCode);

        if(cachedUrl != null) {

            recordClick(shortCode, request);

            clickCounterService.increment(shortCode);

            return cachedUrl;
        }

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException(
                                "Short URL not found"
                        )
                );

        if(!url.isActive()) {
            throw new UrlNotFoundException(
                    "Short URL is inactive"
            );
        }

        if(url.getExpiresAt() != null &&
            !url.getExpiresAt().isAfter(LocalDateTime.now())) {

            throw new UrlExpiredException();
        }

        cacheUrl(url);

        recordClick(shortCode, request);

        clickCounterService.increment(shortCode);

        return url.getOriginalUrl();
    }
}
