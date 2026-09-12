package dev.rudyevhenii.crypto_aggregator.price_alert.dto;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Builder
public record PriceAlertUpdateRequest(
        BigDecimal targetPrice,
        ConditionType conditionType,
        boolean recurring,
        int cooldownMinutes,
        Set<DeliveryMethod> deliveryMethods,
        Instant expiresAt
) {
}
