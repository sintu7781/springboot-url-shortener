package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UrlRepository urlRepository;

    private final UrlClickRepository urlClickRepository;

    public UrlAnalyticsResponse getUrlAnalytics(
            String shortCode
    ) {

        Url url = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "URL not found: " + shortCode
                        )
                );

        long totalClicks =
                urlClickRepository.countByUrlId(
                        url.getId()
                );

        return UrlAnalyticsResponse.builder()
                .shortCode(url.getShortCode())
                .totalClicks(totalClicks)
                .lastClickedAt(
                        urlClickRepository.findLastClickedAt(
                                url.getId()
                        )
                )
                .build();
    }
}
