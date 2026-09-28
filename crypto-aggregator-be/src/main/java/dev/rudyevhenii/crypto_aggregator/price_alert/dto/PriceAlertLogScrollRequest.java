package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import lombok.Builder;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.UUID;

@Builder
@FieldNameConstants
public record PriceAlertLogScrollRequest(
        Instant lastCreatedAt,
        UUID lastId,
        int limit
) {
}
