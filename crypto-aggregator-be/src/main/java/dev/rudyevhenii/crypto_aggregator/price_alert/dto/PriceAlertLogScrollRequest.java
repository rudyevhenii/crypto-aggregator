package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record PriceAlertLogScrollRequest(
        Instant lastCreatedAt,
        UUID lastId,
        int limit
) {
}
