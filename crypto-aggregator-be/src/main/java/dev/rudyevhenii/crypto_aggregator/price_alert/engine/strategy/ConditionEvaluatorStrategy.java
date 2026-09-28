package dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;

import java.math.BigDecimal;

public interface ConditionEvaluatorStrategy {

    boolean shouldTrigger(PriceAlert priceAlert, BigDecimal livePrice);

    ConditionType getConditionType();
}
