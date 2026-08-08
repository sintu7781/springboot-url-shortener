package io.github.sintu7781.urlshortener.service;

import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.exception.UrlExpiredException;
import io.github.sintu7781.urlshortener.exception.UrlNotFoundException;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
//import io.github.sintu7781.urlshortener.service.id.IdGenerator;
import io.github.sintu7781.urlshortener.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService{

    private final UrlRepository urlRepository;
//    private final IdGenerator idGenerator;

    @Transactional
    @Override
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {

        Url existing = urlRepository.findByOriginalUrl(request.getUrl())
                .orElse(null);

        if(existing != null) {
            return UrlResponse.builder()
                    .id(existing.getId())
                    .originalUrl(existing.getOriginalUrl())
                    .shortCode(existing.getShortCode())
                    .shortUrl("http://localhost:8080/" + existing.getShortCode())
                    .clickCount(existing.getClickCount())
                    .build();
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

        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(shortCode)
                .shortUrl("http://localhost:8080/" + shortCode)
                .build();
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
