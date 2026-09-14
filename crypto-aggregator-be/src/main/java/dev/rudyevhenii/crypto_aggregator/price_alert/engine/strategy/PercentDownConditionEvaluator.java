package dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.PercentagePayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;

@Component
public class PercentDownConditionEvaluator implements ConditionEvaluatorStrategy {

    @Override
    public boolean shouldTrigger(PriceAlert priceAlert, BigDecimal livePrice) {
        PercentagePayload payload = (PercentagePayload) priceAlert.getConditionPayload();
        BigDecimal percentage = (payload.getInitialPrice().subtract(livePrice))
                .divide(payload.getInitialPrice(), MathContext.DECIMAL64)
                .movePointRight(2);
        return percentage.compareTo(payload.getPercentageChange()) >= 0;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.PERCENT_DOWN;
    }
}
