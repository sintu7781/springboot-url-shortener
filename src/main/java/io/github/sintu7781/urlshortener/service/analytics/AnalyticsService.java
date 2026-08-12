package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsRangeResponse;
import io.github.sintu7781.urlshortener.dto.response.UrlAnalyticsResponse;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

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

    public UrlAnalyticsRangeResponse getUrlAnalytics(
            String shortCode,
            Instant from,
            Instant to
    ) {

        if(from == null || to == null) {

            throw new IllegalArgumentException(
                    "Both from and to are required."
            );
        }

        if(!from.isBefore(to)) {

            throw new IllegalArgumentException(
                    "'from' must be before 'to'."
            );
        }

        Url url = urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "URL not found: " + shortCode
                        )
                );

        long clicks =
                urlClickRepository
                        .countByUrlIdAndClickedAtBetween(
                                url.getId(),
                                from,
                                to
                        );

        return UrlAnalyticsRangeResponse.builder()
                .shortCode(url.getShortCode())
                .from(from)
                .to(to)
                .clicks(clicks)
                .build();
    }
}
