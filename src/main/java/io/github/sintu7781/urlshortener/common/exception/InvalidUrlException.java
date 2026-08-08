package io.github.sintu7781.urlshortener.common.exception;

public class InvalidUrlException extends RuntimeException{

    public InvalidUrlException() {
        super("Invalid URL.");
    }

    public InvalidUrlException(String message) {
        super(message);
    }
}
