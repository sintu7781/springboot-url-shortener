package io.github.sintu7781.urlshortener.service.impl;

import io.github.sintu7781.urlshortener.common.exception.InvalidUrlException;
import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.common.exception.UrlExpiredException;
import io.github.sintu7781.urlshortener.common.exception.UrlNotFoundException;
import io.github.sintu7781.urlshortener.mapper.UrlMapper;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
//import io.github.sintu7781.urlshortener.service.id.IdGenerator;
import io.github.sintu7781.urlshortener.common.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
//    private final IdGenerator idGenerator;
    private final UrlMapper mapper;

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

    @Transactional
    @Override
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {

        validateUrl(request.getUrl());

        Url existing = urlRepository.findByOriginalUrl(request.getUrl())
                .orElse(null);

        if(existing != null) {
            return mapper.toResponse(existing);
        }

        if(request.getExpiresAt() != null &&
        request.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Expiration time must be in the future."
            );
        }

        long id = urlRepository.getNextId();
//        long id = idGenerator.nextId();

        String shortCode = Base62Generator.encode(id);

        Url url = Url.builder()
                .id(id)
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .expiresAt(request.getExpiresAt())
                .build();

        urlRepository.save(url);

        return mapper.toResponse(url);
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("Short URL not found"));

        if(!url.isActive()) {
            throw new UrlNotFoundException("Short URL is inactive");
        }

        if(url.getExpiresAt() != null &&
        url.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new UrlExpiredException();
        }

        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);

        return url.getOriginalUrl();
    }
}
