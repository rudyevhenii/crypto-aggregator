package dev.rudyevhenii.crypto_aggregator.core.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtTokenException extends AuthenticationException {
    public InvalidJwtTokenException(String message) {
        super(message);
    }
}
