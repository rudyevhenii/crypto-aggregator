package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record PriceAlertUpdateRequest(
        TriggerPolicy triggerPolicy,
        Set<DeliveryMethod> deliveryMethods,
        ConditionPayload conditionPayload,
        Instant expiresAt
) {
}
