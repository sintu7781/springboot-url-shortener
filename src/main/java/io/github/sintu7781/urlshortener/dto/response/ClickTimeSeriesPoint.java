package io.github.sintu7781.urlshortener.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ClickTimeSeriesPoint(
        LocalDate date,
        long clicks
) {
}
