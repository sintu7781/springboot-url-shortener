package io.github.sintu7781.urlshortener.common.exception;

public class ShortCodeAlreadyExistsException extends RuntimeException{

    public ShortCodeAlreadyExistsException( String message) {
        super(message);
    }
}
