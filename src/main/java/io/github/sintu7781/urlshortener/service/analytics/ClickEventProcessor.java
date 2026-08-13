package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.dto.event.ClickEvent;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.entity.UrlClick;
import io.github.sintu7781.urlshortener.repository.UrlClickRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickEventProcessor {

    private final UrlRepository urlRepository;

    private final UrlClickRepository urlClickRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final UserAgentParserService userAgentParserService;

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

            throw new IllegalStateException(
                    "URL not found for shortCode: "
                        + event.shortCode()
            );
        }

        UserAgentInfo userAgentInfo =
                userAgentParserService.parse(
                        event.userAgent()
                );

        UrlClick click = UrlClick.builder()
                .eventId(event.eventId())
                .url(url)
                .clickedAt(event.clickedAt())
                .ipAddress(event.ipAddress())
                .userAgent(event.userAgent())
                .browser(userAgentInfo.browser())
                .operatingSystem(
                        userAgentInfo.operatingSystem()
                )
                .deviceType(
                        userAgentInfo.deviceType()
                )
                .referrer(event.referrer())
                .build();

        urlClickRepository.save(click);

        eventPublisher.publishEvent(
                new AnalyticsCacheInvalidationEvent(
                        event.shortCode()
                )
        );

    }
}
