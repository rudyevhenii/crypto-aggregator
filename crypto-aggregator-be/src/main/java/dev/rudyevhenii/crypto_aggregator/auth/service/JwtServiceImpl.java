package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.core.exception.InvalidJwtTokenException;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${security.jwt.secret-key}")
    private String secretToken;

    @Override
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    @Override
    public String extractSubject(String token) {
        return extractClaims(Claims::getSubject, token);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaims(Claims::getExpiration, token);
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        return !isTokenExpired(token) && user.getEmail().equals(extractSubject(token));
    }

    private String generateToken(User user, long expiration) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(expiration, ChronoUnit.MILLIS)))
                .signWith(signWithSecretKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(Claims::getExpiration, token)
                .before(Date.from(Instant.now()));
    }

    private <T> T extractClaims(Function<Claims, T> claimsFunc, String token) {
        Claims payload;
        try {
            payload = Jwts.parser()
                    .verifyWith(signWithSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenExpirationException("Jwt token has expired");
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("Invalid Jwt token");
        }

        return claimsFunc.apply(payload);
    }

    private SecretKey signWithSecretKey() {
        byte[] keyBytes = secretToken.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
