package dev.rudyevhenii.crypto_aggregator.auth.dto;

import lombok.Builder;

@Builder
public record LogoutRequest(String accessToken, String refreshToken) {
}
