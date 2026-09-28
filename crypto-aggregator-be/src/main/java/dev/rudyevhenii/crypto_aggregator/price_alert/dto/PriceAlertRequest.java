package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.ConditionPayload;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.trigger_policy.TriggerPolicy;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
public record PriceAlertRequest(
        Exchange exchange,
        TradingPair tradingPair,
        TriggerPolicy triggerPolicy,
        Set<DeliveryMethod> deliveryMethods,
        ConditionPayload conditionPayload,
        Instant expiresAt
) {
}
