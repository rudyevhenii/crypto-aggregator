package dev.rudyevhenii.crypto_aggregator.auth.dto;

import lombok.Builder;

@Builder
public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
}
