package dev.rudyevhenii.crypto_aggregator.auth.dto;

public record LogoutRequest(String accessToken, String refreshToken) {
}
