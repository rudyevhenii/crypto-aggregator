package dev.rudyevhenii.crypto_aggregator.auth.dto;

import lombok.Builder;

@Builder
public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {
}
