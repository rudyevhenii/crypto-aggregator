package dev.rudyevhenii.crypto_aggregator.utils;

import dev.rudyevhenii.crypto_aggregator.auth.service.JwtService;
import dev.rudyevhenii.crypto_aggregator.auth.service.TokenType;
import io.jsonwebtoken.Jwts;
import io.restassured.http.Header;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpHeaders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils.TestResources.*;

@UtilityClass
public class JwtTokenUtils {

    public final String SECRET_KEY = "zHj96Gp6iHQwMsXUrD7Pfue31edAvoyTkoeBsIKeU1JUlcZ1rCLTVc8Z3fmq7J6D";

    public Header buildAuthHeader(UUID userId) {
        return new Header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId));
    }

    public String buildAccessToken(UUID userId) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, NOW, ACCESS_TOKEN_EXP, SECRET_KEY);
    }

    public String buildAccessToken(UUID userId, Instant expiration) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, NOW, expiration, SECRET_KEY);
    }

    public String buildAccessToken(UUID userId, Instant issuedAt, Instant expiration) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, issuedAt, expiration, SECRET_KEY);
    }

    public String buildRefreshToken(UUID userId) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, NOW, REFRESH_TOKEN_EXP, SECRET_KEY);
    }

    public String buildRefreshToken(UUID userId, Instant expiration) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, NOW, expiration, SECRET_KEY);
    }

    public String buildRefreshToken(UUID userId, Instant issuedAt, Instant expiration) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, issuedAt, expiration, SECRET_KEY);
    }

    public String buildExpiredAccessToken(UUID userId) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, buildExpiredDate(), buildExpiredDate(), SECRET_KEY);
    }

    public String buildExpiredRefreshToken(UUID userId) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, buildExpiredDate(), buildExpiredDate(), SECRET_KEY);
    }

    public String buildCorruptedAccessToken(UUID userId, Instant expiration) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, NOW, expiration, CORRUPTED_SECRET_KEY);
    }

    public String buildCorruptedAccessToken(UUID userId) {
        return buildToken(userId, TokenType.ACCESS_TOKEN, NOW, ACCESS_TOKEN_EXP, CORRUPTED_SECRET_KEY);
    }

    public String buildCorruptedRefreshToken(UUID userId, Instant expiration) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, NOW, expiration, CORRUPTED_SECRET_KEY);
    }

    public String buildCorruptedRefreshToken(UUID userId) {
        return buildToken(userId, TokenType.REFRESH_TOKEN, NOW, REFRESH_TOKEN_EXP, CORRUPTED_SECRET_KEY);
    }

    private Instant buildExpiredDate() {
        return NOW.minus(1, ChronoUnit.HOURS);
    }

    private String buildToken(UUID userId, TokenType tokenType, Instant issuedAt, Instant expiration, String secretKey) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(JwtService.signWithSecretKey(secretKey))
                .compact();
    }

    static class TestResources {
        static final String TOKEN_TYPE = JwtService.TOKEN_TYPE;

        static final Instant NOW = Instant.now();
        static final Instant ACCESS_TOKEN_EXP = NOW.plus(60, ChronoUnit.MINUTES);
        static final Instant REFRESH_TOKEN_EXP = NOW.plus(7, ChronoUnit.DAYS);

        static final String CORRUPTED_SECRET_KEY = "corrupted-secret-key-invalid-signature-for-testing-purposes-1234";
    }
}
