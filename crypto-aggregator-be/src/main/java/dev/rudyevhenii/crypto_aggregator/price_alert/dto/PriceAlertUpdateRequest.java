package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.ConditionPayload;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record PriceAlertUpdateRequest(
        boolean recurring,
        int cooldownMinutes,
        Set<DeliveryMethod> deliveryMethods,
        ConditionPayload conditionPayload,
        Instant expiresAt
) {
}
