package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClickCountSyncService {

    private final UrlRepository urlRepository;

    private final ClickCounterService clickCounterService;

    @Transactional
    public void sync(String shortCode) {

        Long clicks = clickCounterService.getAndReset(shortCode);

        if(clicks == 0) {
            return;
        }

        Url url = urlRepository.findByShortCode(shortCode)
                .orElse(null);

        if(url == null) {
            return;
        }

        url.setClickCount(
                url.getClickCount() + clicks
        );

        urlRepository.save(url);

    }
}
