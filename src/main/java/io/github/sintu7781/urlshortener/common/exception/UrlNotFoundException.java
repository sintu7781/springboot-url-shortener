package io.github.sintu7781.urlshortener.exception;

public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException() {
        super("This URL is not found.");
    }

    public UrlNotFoundException(String message) {
        super(message);
    }
}
