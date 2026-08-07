package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public interface JwtService {

    String TOKEN_TYPE = "type";

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    UUID extractSubject(String token);

    boolean isTokenValid(String token, User user);

    Date extractExpiration(String token);

    TokenType extractTokenType(String token);

    static SecretKey signWithSecretKey(String secretToken) {
        byte[] keyBytes = secretToken.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
