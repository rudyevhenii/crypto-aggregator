package dev.rudyevhenii.crypto_aggregator.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface JwtService {

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    UUID extractSubject(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
