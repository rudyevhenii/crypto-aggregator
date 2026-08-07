package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.domain.User;
import dev.rudyevhenii.crypto_aggregator.core.exception.InvalidJwtTokenException;
import dev.rudyevhenii.crypto_aggregator.core.exception.JwtTokenExpirationException;
import dev.rudyevhenii.crypto_aggregator.core.util.GeneratorUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    private final String secretKey;
    
    private final GeneratorUtils generator;

    public JwtServiceImpl(@Value("${security.jwt.access-token-expiration}") long accessTokenExpiration,
                          @Value("${security.jwt.refresh-token-expiration}") long refreshTokenExpiration,
                          @Value("${security.jwt.secret-key}") String secretKey,
                          GeneratorUtils generator) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.secretKey = secretKey;
        this.generator = generator;
    }

    @Override
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration, TokenType.ACCESS_TOKEN);
    }

    @Override
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration, TokenType.REFRESH_TOKEN);
    }

    @Override
    public UUID extractSubject(String token) {
        return UUID.fromString(extractClaims(Claims::getSubject, token));
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaims(Claims::getExpiration, token);
    }

    @Override
    public TokenType extractTokenType(String token) {
        return TokenType.valueOf(extractClaims(claims -> claims.get(TOKEN_TYPE, String.class), token));
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        return !isTokenExpired(token) && user.getId().equals(extractSubject(token));
    }

    private String generateToken(User user, long expiration, TokenType tokenType) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(generator.now()))
                .expiration(Date.from(generator.now().plus(expiration, ChronoUnit.MILLIS)))
                .signWith(JwtService.signWithSecretKey(secretKey))
                .compact();
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractClaims(Claims::getExpiration, token)
                    .before(Date.from(generator.now()));
        } catch (AuthenticationException e) {
            return true;
        }
    }

    private <T> T extractClaims(Function<Claims, T> claimsFunc, String token) {
        Claims payload;
        try {
            payload = Jwts.parser()
                    .verifyWith(JwtService.signWithSecretKey(secretKey))
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
}
