package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;

import java.util.Date;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    String extractSubject(String token);

    boolean isTokenValid(String token, User user);

    Date extractExpiration(String token);

    TokenType extractTokenType(String token);
}
