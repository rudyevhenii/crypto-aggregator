package dev.rudyevhenii.crypto_aggregator.core.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtTokenExpirationException extends AuthenticationException {
    public JwtTokenExpirationException(String message) {
        super(message);
    }
}
