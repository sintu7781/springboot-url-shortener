package io.github.sintu7781.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateShortUrlRequest {

    @NotBlank(message = "URL is required")
    private String url;

    @Pattern(
            regexp = "^[a-zA-Z0-9_-]{3,30}$",
            message =
                    "Custom alias must contain 3-30 characters and only letters, number, '_' or '-'."
    )
    private String customAlias;

    private LocalDateTime expiresAt;
}
