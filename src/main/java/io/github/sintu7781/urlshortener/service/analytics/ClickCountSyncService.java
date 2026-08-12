package io.github.sintu7781.urlshortener.service.analytics;

import io.github.sintu7781.urlshortener.entity.ClickCountSyncBatch;
import io.github.sintu7781.urlshortener.entity.Url;
import io.github.sintu7781.urlshortener.repository.ClickCountSyncBatchRepository;
import io.github.sintu7781.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClickCountSyncService {

    private final UrlRepository urlRepository;

    private final ClickCounterService clickCounterService;

    private final ClickCountSyncBatchRepository batchRepository;

    @Transactional
    public boolean sync(
            String shortCode,
            String rotatedKey
    ) {

        Url url = urlRepository
                .findByShortCode(shortCode)
                .orElse(null);

        if(url == null) {
            return false;
        }

        Long clicks =
                clickCounterService.getRotatedCount(rotatedKey);

        if(clicks <= 0) {
            return true;
        }

        if(batchRepository.existsById(rotatedKey)) {
            return true;
        }

        ClickCountSyncBatch batch =
                ClickCountSyncBatch.builder()
                        .batchKey(rotatedKey)
                        .shortCode(shortCode)
                        .clickCount(clicks)
                        .processedAt(LocalDateTime.now())
                        .build();

        batchRepository.save(batch);

        url.setClickCount(
                url.getClickCount() + clicks
        );

        urlRepository.save(url);

        return true;
    }
}
