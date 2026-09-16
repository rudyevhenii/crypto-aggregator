package dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.TargetPricePayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GreaterThanConditionEvaluator implements ConditionEvaluatorStrategy {

    @Override
    public boolean shouldTrigger(PriceAlert priceAlert, BigDecimal livePrice) {
        TargetPricePayload payload = (TargetPricePayload) priceAlert.getConditionPayload();
        return livePrice.compareTo(payload.getTargetPrice()) > 0;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.GREATER_THAN;
    }
}
