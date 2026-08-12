package io.github.sintu7781.urlshortener.repository.projection;

import java.time.Instant;

public interface ClickHourlyTimeSeriesProjection {

    Instant getHour();

    Long getClicks();
}
