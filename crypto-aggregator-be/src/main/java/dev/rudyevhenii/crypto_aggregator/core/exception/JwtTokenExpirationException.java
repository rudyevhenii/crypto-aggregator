package dev.rudyevhenii.crypto_aggregator.core.exception;

public class JwtTokenExpirationException extends RuntimeException {
    public JwtTokenExpirationException(String message) {
        super(message);
    }
}
