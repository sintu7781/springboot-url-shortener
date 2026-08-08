package io.github.sintu7781.urlshortener.exception;

public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException() {
        super("This short URL has expired.");
    }

    public UrlExpiredException(String message) {
        super(message);
    }
}
