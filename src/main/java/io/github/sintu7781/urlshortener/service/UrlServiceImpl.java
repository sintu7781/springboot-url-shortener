package io.github.sintu7781.urlshortener.service;

import io.github.sintu7781.urlshortener.dto.request.CreateShortUrlRequest;
import io.github.sintu7781.urlshortener.dto.response.UrlResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.exception.UrlNotFoundException;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import io.github.sintu7781.urlshortener.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService{

    private final UrlRepository urlRepository;

    @Override
    public UrlResponse createShortUrl(CreateShortUrlRequest request) {

        String shortCode;

        do {
            shortCode = Base62Generator.generate(6);
        } while (urlRepository.existsByShortCode(shortCode));

        Url url = Url.builder()
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .build();

        Url saved = urlRepository.save(url);

        return UrlResponse.builder()
                .id(saved.getId())
                .originalUrl(saved.getOriginalUrl())
                .shortCode(saved.getShortCode())
                .shortUrl("http://localhost:8080/" + saved.getShortCode())
                .build();
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("Short URL not found"));

        return url.getOriginalUrl();
    }
}
