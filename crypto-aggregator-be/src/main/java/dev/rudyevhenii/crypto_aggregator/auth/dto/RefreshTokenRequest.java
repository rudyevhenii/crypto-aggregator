package dev.rudyevhenii.crypto_aggregator.auth.dto;

import lombok.Builder;

@Builder
public record RefreshTokenRequest(
        String refreshToken
) {
}
