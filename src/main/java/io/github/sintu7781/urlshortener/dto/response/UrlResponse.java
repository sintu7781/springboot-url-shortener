package io.github.sintu7781.urlshortener.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UrlResponse {

    private Long id;

    private String originalUrl;

    private String shortCode;

    private String shortUrl;
}
