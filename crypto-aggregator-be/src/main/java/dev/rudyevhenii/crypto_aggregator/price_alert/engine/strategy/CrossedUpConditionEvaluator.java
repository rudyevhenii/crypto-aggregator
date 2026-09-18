package dev.rudyevhenii.crypto_aggregator.price_alert.engine.strategy;

import dev.rudyevhenii.crypto_aggregator.price_alert.ConditionType;
import dev.rudyevhenii.crypto_aggregator.price_alert.domain.PriceAlert;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.PriceAlertStateStorageService;
import dev.rudyevhenii.crypto_aggregator.price_alert.engine.json.condition_payload.TargetPricePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CrossedUpConditionEvaluator implements ConditionEvaluatorStrategy {

    private final PriceAlertStateStorageService priceAlertStateStorageService;

    @Override
    public boolean shouldTrigger(PriceAlert priceAlert, BigDecimal livePrice) {
        BigDecimal previousPrice = priceAlertStateStorageService.getPreviousPrice(priceAlert.getId());
        priceAlertStateStorageService.updatePreviousPriceAsync(priceAlert.getId(), livePrice);

        if (previousPrice == null) return false;
        TargetPricePayload payload = (TargetPricePayload) priceAlert.getConditionPayload();

        return payload.getTargetPrice().compareTo(previousPrice) >= 0 &&
                livePrice.compareTo(payload.getTargetPrice()) > 0;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.CROSSED_UP;
    }
}
