package io.github.sintu7781.urlshortener.service.analytics;

import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

@Service
public class AnalyticsTimeZoneService {

    private static final String DEFAULT_TIMEZONE = "UTC";

    public ZoneId resolve(String timezone) {

        if(timezone == null || timezone.isEmpty()) {

            return ZoneId.of(DEFAULT_TIMEZONE);
        }

        try {

            return ZoneId.of(timezone);

        } catch (DateTimeException ex) {

            throw new IllegalArgumentException(
                    "Invalid timezone: " + timezone
            );
        }
    }

    public AnalyticsContext createContext(
            Instant from,
            Instant to,
            String timezone
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

        return new AnalyticsContext(
                from,
                to,
                resolve(timezone)
        );
    }
}
