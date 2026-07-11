package dev.rudyevhenii.crypto_aggregator.auth.service;

import dev.rudyevhenii.crypto_aggregator.auth.dto.LoginRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RefreshTokenRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.RegisterRequest;
import dev.rudyevhenii.crypto_aggregator.auth.dto.TokenResponseDto;

public interface AuthService {

    TokenResponseDto register(RegisterRequest registerRequest);

    TokenResponseDto login(LoginRequest loginRequest);

    TokenResponseDto refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(String token);
}
