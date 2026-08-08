package io.github.sintu7781.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateShortUrlRequest {

    @NotBlank(message = "URL is required")

    private String url;

    private LocalDateTime expiresAt;
}
