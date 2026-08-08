package io.github.sintu7781.urlshortener.common.exception;

public class InvalidExpirationException extends RuntimeException{

    public InvalidExpirationException(String message) {
        super(message);
    }
}
