package io.github.sintu7781.urlshortener.service.analytics;

import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class AnalyticsTimeZoneService {

    public ZoneId resolve(String timezone) {

        if(timezone == null || timezone.isEmpty()) {

            return ZoneId.of("UTC");
        }

        try {

            return ZoneId.of(timezone);

        } catch (DateTimeException ex) {

            throw new IllegalArgumentException(
                    "Invalid timezone: " + timezone
            );
        }
    }
}
