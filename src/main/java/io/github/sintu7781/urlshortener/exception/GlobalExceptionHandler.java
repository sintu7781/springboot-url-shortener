package io.github.sintu7781.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUrlNotFound(UrlNotFoundException ex) {
        return Map.of(
                "message", ex.getMessage()
        );
    }

    @ExceptionHandler(UrlExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public Map<String, String> handleUrlExpired(UrlExpiredException ex) {
        return Map.of(
                "message", ex.getMessage()
        );
    }
}
