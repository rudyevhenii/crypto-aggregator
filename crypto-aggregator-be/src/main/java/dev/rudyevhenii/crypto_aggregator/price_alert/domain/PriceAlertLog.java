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
public class PriceAlertLog {
    private UUID id;
    private UUID priceAlertId;
    private UUID userId;
    private Exchange exchange;
    private TradingPair tradingPair;
    private ConditionType conditionType;
    private BigDecimal triggeredPrice;
    private String message;
    @Builder.Default
    private Set<DeliveryMethod> deliveryMethods = new HashSet<>();
    private Instant createdAt;
}
