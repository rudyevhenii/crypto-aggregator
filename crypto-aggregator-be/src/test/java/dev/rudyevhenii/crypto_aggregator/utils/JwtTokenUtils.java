package dev.rudyevhenii.crypto_aggregator.utils;

import dev.rudyevhenii.crypto_aggregator.auth.service.TokenType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restassured.http.Header;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils.TestResources.INFINITE_EXPIRATION;
import static dev.rudyevhenii.crypto_aggregator.utils.JwtTokenUtils.TestResources.TOKEN_TYPE;

@Component
public class JwtTokenUtils {

    @Value("${security.jwt.secret-key}")
    private String secretToken;

    public Header buildAuthHeader(UUID userId) {
        return new Header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId));
    }

    public String buildAccessToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(TOKEN_TYPE, TokenType.ACCESS_TOKEN)
                .issuedAt(Date.from(Instant.now()))
                .expiration(INFINITE_EXPIRATION)
                .signWith(signWithSecretKey())
                .compact();
    }

    public String buildRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim(TOKEN_TYPE, TokenType.REFRESH_TOKEN)
                .issuedAt(Date.from(Instant.now()))
                .expiration(INFINITE_EXPIRATION)
                .signWith(signWithSecretKey())
                .compact();
    }

    public static String buildCorruptedAccessToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLCJ0e" +
                "XBlIjoiQUNDRVNTX1RPS0VOIiwiaWF0IjoxNzY3MjI1NjAwLCJleHAiOjE5MDAwMDAwMDB9.Z2kCNnXm9SMhbD" +
                "piGfiWLGCYyaYC20FUrBtelZxtWJiTs9wusr0tWq5diTJSHfYb2zEFH9eUb7QLomrgRE3e7Q";
    }

    public static String buildExpiredAccessToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLC" +
                "J0eXBlIjoiQUNDRVNTX1RPS0VOIiwiaWF0IjoxNzY3MjI1NjAwLCJleHAiOjE3NjczMTIwMDB9.3m-bQ0L" +
                "sOXXF3jpBnKZYA4qdY8x1rWZtWE95ceMGhwqcwDlhxpYseEcoG811NITLxZhnMgE-nVbogiApoZqA1Q";
    }

    public static String buildValidRefreshToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLCJ0e" +
                "XBlIjoiUkVGUkVTSF9UT0tFTiIsImlhdCI6MTc4NTMwNjQyMywiZXhwIjoxNzg1OTExMjIzfQ.lFXe0UJZ0MVy" +
                "aLfWNSq4139ZsicZsKTSHxZBqc2_M_WhqgsrqcEsyUYuxI6E1nn-C60oGYxR4UuWQMFxWQcq-w";
    }

    private SecretKey signWithSecretKey() {
        byte[] keyBytes = secretToken.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    static class TestResources {
        static final String TOKEN_TYPE = "type";
        static final Date INFINITE_EXPIRATION = new Date(Long.MAX_VALUE);
    }
}
