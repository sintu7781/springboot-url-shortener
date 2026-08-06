package io.github.sintu7781.urlshortener.controller;

import io.github.sintu7781.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {

        String originalUrl = urlService.getOriginalUrl(shortCode);

        return ResponseEntity
                .status(302)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }
}
