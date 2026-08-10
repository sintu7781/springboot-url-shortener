package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.entity.UrlClick;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickEventProcessor {

    private final UrlRepository urlRepository;

    private final UrlClickRepository urlClickRepository;

    @Transactional
    public void process(ClickEvent event) {

        if(urlClickRepository.existsByEventId(
                event.eventId()
        )) {

            return;
        }

        Url url = urlRepository
                .findByShortCode(event.shortCode())
                .orElse(null);

        if(url == null) {
            return;
        }

        UrlClick click = UrlClick.builder()
                .eventId(event.eventId())
                .url(url)
                .clickedAt(event.clickedAt())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .referrer(event.referrer())
                .build();

        urlClickRepository.save(click);

    }
}
