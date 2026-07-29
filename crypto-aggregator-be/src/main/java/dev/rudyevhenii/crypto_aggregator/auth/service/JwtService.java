package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;

import java.util.Date;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    UUID extractSubject(String token);

    boolean isTokenValid(String token, User user);

    Date extractExpiration(String token);

    TokenType extractTokenType(String token);
}
