package io.github.sintu7781.urlshortener.repository.projection;

import java.time.LocalDate;

public interface ClickTimeSeriesProjection {

    LocalDate getDate();

    Long getClicks();
}
