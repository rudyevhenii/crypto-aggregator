package dev.rudyevhenii.crypto_aggregator.price_alert.domain;

import dev.rudyevhenii.crypto_aggregator.core.enums.Exchange;
import dev.rudyevhenii.crypto_aggregator.core.enums.TradingPair;
import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.DeliveryMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {
    private UUID id;
    private UUID userId;
    private Exchange exchange;
    private TradingPair tradingPair;
    private BigDecimal targetPrice;
    private ConditionType conditionType;
    @Builder.Default
    private boolean recurring = false;
    @Builder.Default
    private int cooldownMinutes = 1;
    @Builder.Default
    private Set<DeliveryMethod> deliveryMethods = new HashSet<>();
    @Builder.Default
    private boolean active = true;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
